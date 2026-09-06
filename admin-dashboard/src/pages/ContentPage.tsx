import { Fragment, useState } from "react";
import { collection, orderBy, query } from "firebase/firestore";
import { db } from "../firebase";
import { useCollection } from "../lib/useCollection";
import { StatusPill, EmptyState, formatDate } from "../components/Common";
import { Material } from "../types";
import { adminApi } from "../lib/adminApi";

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

  const safeMaterials = Array.isArray(materials) ? materials : [];

  const visible =
    filter === "ALL"
      ? safeMaterials
      : safeMaterials.filter((m) => m?.status === "PENDING_REVIEW");

  const approve = async (targetId: string) => {
    setBusyId(targetId);
    try {
      await adminApi.approveMaterial(targetId);
    } catch (err: any) {
      alert("Error approving material: " + (err.message || err));
    } finally {
      setBusyId(null);
    }
  };

  const submitReject = async (targetId: string) => {
    if (!reason.trim()) {
      alert("Tafadhali weka sababu ya kukataa nyenzo hii.");
      return;
    }
    setBusyId(targetId);
    try {
      await adminApi.rejectMaterial(targetId, reason.trim());
      setRejectingId(null);
      setReason("");
    } catch (err: any) {
      alert("Error rejecting material: " + (err.message || err));
    } finally {
      setBusyId(null);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Section */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-gray-200 pb-5">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Uhakiki wa Maudhui (Content)</h1>
          <p className="text-sm text-gray-500 mt-1">
            Kagua na uthibitishe nyenzo zilizopakiwa na walimu kabla hazijafika kwenye maktaba.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            className={`px-4 py-2 text-xs font-semibold rounded-xl transition ${
              filter === "PENDING_REVIEW"
                ? "bg-emerald-600 text-white shadow-sm"
                : "bg-gray-100 text-gray-600 hover:bg-gray-200"
            }`}
            onClick={() => setFilter("PENDING_REVIEW")}
          >
            Zinazosubiri (Pending)
          </button>
          <button
            className={`px-4 py-2 text-xs font-semibold rounded-xl transition ${
              filter === "ALL"
                ? "bg-emerald-600 text-white shadow-sm"
                : "bg-gray-100 text-gray-600 hover:bg-gray-200"
            }`}
            onClick={() => setFilter("ALL")}
          >
            Zote (All)
          </button>
        </div>
      </div>

      {/* Content Table */}
      {loading ? (
        <EmptyState text="Inapakia maudhui..." />
      ) : visible.length === 0 ? (
        <EmptyState text="Hakuna nyenzo zozote zinazosubiri ukaguzi." />
      ) : (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-100 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  <th className="py-3.5 px-4">Kichwa (Title)</th>
                  <th className="py-3.5 px-4">Mwalimu</th>
                  <th className="py-3.5 px-4">Kidato / Somo</th>
                  <th className="py-3.5 px-4">Tarehe</th>
                  <th className="py-3.5 px-4">Hali (Status)</th>
                  <th className="py-3.5 px-4 text-right">Vitendo</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50 text-sm text-gray-700">
                {visible.map((m) => {
                  // Fallback salama ya kuchukua document ID kama materialId haipo
                  const targetId = m.materialId || (m as any).id;

                  return (
                    <Fragment key={targetId || Math.random().toString()}>
                      <tr className="hover:bg-gray-50/50 transition-colors">
                        <td className="py-3.5 px-4 font-medium text-gray-900">
                          {m.fileUrl ? (
                            <a
                              href={m.fileUrl}
                              target="_blank"
                              rel="noreferrer"
                              className="text-emerald-700 hover:underline"
                            >
                              {m.title || "Untitled"}
                            </a>
                          ) : (
                            m.title || "Untitled"
                          )}
                        </td>
                        <td className="py-3.5 px-4 text-gray-600">{m.teacherName || m.teacherId || "N/A"}</td>
                        <td className="py-3.5 px-4 text-gray-500">
                          {m.form ? m.form.replace("_", " ") : "N/A"} · {m.subject || "N/A"}
                        </td>
                        <td className="py-3.5 px-4 text-xs text-gray-500">
                          {m.createdAt ? formatDate(m.createdAt) : "N/A"}
                        </td>
                        <td className="py-3.5 px-4">
                          <StatusPill status={m.status} />
                        </td>
                        <td className="py-3.5 px-4 text-right">
                          {m.status === "PENDING_REVIEW" && (
                            <div className="flex items-center justify-end gap-2">
                              <button
                                className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-xs font-semibold transition disabled:opacity-50"
                                disabled={busyId === targetId}
                                onClick={() => approve(targetId)}
                              >
                                {busyId === targetId ? "Inasindika..." : "Approve"}
                              </button>
                              <button
                                className="px-3 py-1.5 bg-rose-50 hover:bg-rose-100 text-rose-700 rounded-lg text-xs font-semibold transition disabled:opacity-50"
                                disabled={busyId === targetId}
                                onClick={() => setRejectingId(targetId)}
                              >
                                Reject
                              </button>
                            </div>
                          )}
                        </td>
                      </tr>

                      {/* Dropdown Input Form for Rejection */}
                      {rejectingId === targetId && (
                        <tr className="bg-rose-50/50">
                          <td colSpan={6} className="px-4 py-3">
                            <div className="flex flex-col sm:flex-row items-center gap-3">
                              <input
                                className="flex-1 w-full px-3 py-1.5 rounded-lg border border-rose-200 text-sm outline-none focus:ring-2 focus:ring-rose-500 bg-white"
                                placeholder="Weka sababu ya kukataa (mwalimu ataiona)..."
                                value={reason}
                                onChange={(e) => setReason(e.target.value)}
                              />
                              <div className="flex items-center gap-2 w-full sm:w-auto justify-end">
                                <button
                                  className="px-3 py-1.5 bg-rose-600 hover:bg-rose-700 text-white rounded-lg text-xs font-semibold transition disabled:opacity-50"
                                  disabled={busyId === targetId}
                                  onClick={() => submitReject(targetId)}
                                >
                                  Thibitisha Kukataa
                                </button>
                                <button
                                  className="px-3 py-1.5 bg-gray-200 hover:bg-gray-300 text-gray-700 rounded-lg text-xs font-semibold transition"
                                  onClick={() => {
                                    setRejectingId(null);
                                    setReason("");
                                  }}
                                >
                                  Ghairi
                                </button>
                              </div>
                            </div>
                          </td>
                        </tr>
                      )}
                    </Fragment>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
