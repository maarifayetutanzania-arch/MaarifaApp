import { Fragment, useState } from "react";
import { collection, orderBy, query } from "firebase/firestore";
import { db } from "../firebase";
import { useCollection } from "../lib/useCollection";
import { StatusPill, EmptyState, formatDate } from "../components/Common";
import { Material } from "../types";
import { adminApi } from "../lib/adminApi";

// 1. Query imewkwa nje ya component kuzuia infinite re-render loop
const materialsQuery = query(
  collection(db, "materials"),
  orderBy("createdAt", "desc")
);

export function ContentPage() {
  const { data: materials = [], loading } = useCollection<Material>(materialsQuery);
  const [busyId, setBusyId] = useState<string | null>(null);
  const [rejectingId, setRejectingId] = useState<string | null>(null);
  const [reason, setReason] = useState("");
  const [filter, setFilter] = useState<"ALL" | "PENDING_REVIEW">("PENDING_REVIEW");

  const approve = async (materialId: string) => {
    setBusyId(materialId);
    try {
      await adminApi.approveMaterial(materialId);
    } finally {
      setBusyId(null);
    }
  };

  const submitReject = async (materialId: string) => {
    setBusyId(materialId);
    try {
      await adminApi.rejectMaterial(materialId, reason);
      setRejectingId(null);
      setReason("");
    } finally {
      setBusyId(null);
    }
  };

  // 2. Kinga ya safe array handling kuzuia crash ikitokea data ni undefined
  const safeMaterials = Array.isArray(materials) ? materials : [];

  const visible =
    filter === "ALL"
      ? safeMaterials
      : safeMaterials.filter((m) => m?.status === "PENDING_REVIEW");

  return (
    <div className="main">
      <div className="page-header">
        <div>
          <h1>Content</h1>
          <p>Approve or reject teacher uploads before they reach the library.</p>
        </div>
        <div className="row-actions">
          <button
            className={filter === "PENDING_REVIEW" ? "btn-primary" : "btn-ghost"}
            onClick={() => setFilter("PENDING_REVIEW")}
          >
            Pending
          </button>
          <button
            className={filter === "ALL" ? "btn-primary" : "btn-ghost"}
            onClick={() => setFilter("ALL")}
          >
            All
          </button>
        </div>
      </div>

      {loading ? (
        <EmptyState text="Loading…" />
      ) : visible.length === 0 ? (
        <EmptyState text="Nothing to review." />
      ) : (
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Teacher</th>
              <th>Form / Subject</th>
              <th>Uploaded</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {visible.map((m) => (
              <Fragment key={m.materialId || Math.random().toString()}>
                <tr>
                  <td>
                    {m.fileUrl ? (
                      <a href={m.fileUrl} target="_blank" rel="noreferrer">
                        {m.title || "Untitled"}
                      </a>
                    ) : (
                      m.title || "Untitled"
                    )}
                  </td>
                  <td>{m.teacherName || m.teacherId || "N/A"}</td>
                  <td>
                    {m.form ? m.form.replace("_", " ") : "N/A"} · {m.subject || "N/A"}
                  </td>
                  <td>{m.createdAt ? formatDate(m.createdAt) : "N/A"}</td>
                  <td>
                    <StatusPill status={m.status} />
                  </td>
                  <td>
                    {m.status === "PENDING_REVIEW" && (
                      <div className="row-actions">
                        <button
                          className="btn-primary"
                          disabled={busyId === m.materialId}
                          onClick={() => approve(m.materialId)}
                        >
                          Approve
                        </button>
                        <button
                          className="btn-danger"
                          disabled={busyId === m.materialId}
                          onClick={() => setRejectingId(m.materialId)}
                        >
                          Reject
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
                {rejectingId === m.materialId && (
                  <tr>
                    <td colSpan={6}>
                      <div style={{ display: "flex", gap: 8 }}>
                        <input
                          placeholder="Reason (shown to the teacher)"
                          value={reason}
                          onChange={(e) => setReason(e.target.value)}
                        />
                        <button
                          className="btn-danger"
                          onClick={() => submitReject(m.materialId)}
                        >
                          Confirm reject
                        </button>
                        <button
                          className="btn-ghost"
                          onClick={() => setRejectingId(null)}
                        >
                          Cancel
                        </button>
                      </div>
                    </td>
                  </tr>
                )}
              </Fragment>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
