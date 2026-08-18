import { Fragment, useState } from "react";
import { collection, orderBy, query } from "firebase/firestore";
import { db } from "../firebase";
import { useCollection } from "../lib/useCollection";
import { StatusPill, EmptyState } from "../components/Common";
import { Teacher } from "../types";
import { adminApi } from "../lib/adminApi";

// Query imewekwa nje ya component kuzuia re-creation kwenye kila render
const teachersQuery = query(
  collection(db, "teachers"),
  orderBy("verificationStatus")
);

export function TeachersPage() {
  const { data: teachers, loading } = useCollection<Teacher>(teachersQuery);
  const [busyId, setBusyId] = useState<string | null>(null);
  const [rejectingId, setRejectingId] = useState<string | null>(null);
  const [rejectNotes, setRejectNotes] = useState("");

  const approve = async (teacherId: string) => {
    setBusyId(teacherId);
    try {
      await adminApi.approveTeacher(teacherId);
    } finally {
      setBusyId(null);
    }
  };

  const submitReject = async (teacherId: string) => {
    setBusyId(teacherId);
    try {
      await adminApi.rejectTeacher(teacherId, rejectNotes);
      setRejectingId(null);
      setRejectNotes("");
    } finally {
      setBusyId(null);
    }
  };

  const sorted = [...teachers].sort((a, b) =>
    a.verificationStatus === "PENDING" ? -1 : 1
  );

  return (
    <div className="main">
      <div className="page-header">
        <div>
          <h1>Teachers</h1>
          <p>Review applications and manage verified teacher accounts.</p>
        </div>
      </div>

      {loading ? (
        <EmptyState text="Loading…" />
      ) : sorted.length === 0 ? (
        <EmptyState text="No teacher accounts yet." />
      ) : (
        <table>
          <thead>
            <tr>
              <th>Teacher</th>
              <th>Status</th>
              <th>Uploads</th>
              <th>Engagement score</th>
              <th>Balance</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {sorted.map((t) => (
              <Fragment key={t.teacherId}>
                <tr>
                  <td>{t.teacherId}</td>
                  <td>
                    <StatusPill status={t.verificationStatus} />
                  </td>
                  <td>{t.totalUploads}</td>
                  <td>{t.engagementScore.toFixed(1)}</td>
                  <td>{t.earningsBalanceTzs.toLocaleString()} TZS</td>
                  <td>
                    {t.verificationStatus === "PENDING" && (
                      <div className="row-actions">
                        <button
                          className="btn-primary"
                          disabled={busyId === t.teacherId}
                          onClick={() => approve(t.teacherId)}
                        >
                          Approve
                        </button>
                        <button
                          className="btn-danger"
                          disabled={busyId === t.teacherId}
                          onClick={() => setRejectingId(t.teacherId)}
                        >
                          Reject
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
                {rejectingId === t.teacherId && (
                  <tr>
                    <td colSpan={6}>
                      <div style={{ display: "flex", gap: 8 }}>
                        <input
                          placeholder="Reason for rejection (shown to the teacher)"
                          value={rejectNotes}
                          onChange={(e) => setRejectNotes(e.target.value)}
                        />
                        <button
                          className="btn-danger"
                          onClick={() => submitReject(t.teacherId)}
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
