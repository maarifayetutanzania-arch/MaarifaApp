import { Fragment, useState } from "react";
import { collection, orderBy, query } from "firebase/firestore";
import { db } from "../lib/firebase";
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
    } catch (err: any) {
      alert("Imefeli kuthibitisha mwalimu: " + (err.message || err));
    } finally {
      setBusyId(null);
    }
  };

  const submitReject = async (teacherId: string) => {
    if (!rejectNotes.trim()) {
      alert("Tafadhali weka sababu ya kumkataa mwalimu.");
      return;
    }
    setBusyId(teacherId);
    try {
      await adminApi.rejectTeacher(teacherId, rejectNotes.trim());
      setRejectingId(null);
      setRejectNotes("");
    } catch (err: any) {
      alert("Imefeli kukataa maombi: " + (err.message || err));
    } finally {
      setBusyId(null);
    }
  };

  // Safe sorting bila ku-crash kama teachers bado ni undefined
  const sorted = [...(teachers || [])].sort((a, b) =>
    a.verificationStatus === "PENDING" ? -1 : 1
  );

  return (
    <div className="space-y-6">
      {/* Header Section */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-gray-200 pb-5">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Usimamizi wa Walimu</h1>
          <p className="text-sm text-gray-500 mt-1">
            Kagua maombi mapya na usimamie akaunti zote za walimu zilizothibitishwa.
          </p>
        </div>
      </div>

      {/* Content Section */}
      {loading ? (
        <EmptyState text="Inapakia walimu..." />
      ) : sorted.length === 0 ? (
        <EmptyState text="Hakuna akaunti za walimu zilizopatikana." />
      ) : (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-200 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  <th className="px-6 py-4">Mwalimu</th>
                  <th className="px-6 py-4">Hali (Status)</th>
                  <th className="px-6 py-4">Maudhui (Uploads)</th>
                  <th className="px-6 py-4">Engagement Score</th>
                  <th className="px-6 py-4">Salio (Balance)</th>
                  <th className="px-6 py-4 text-right">Vitendo</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 text-sm text-gray-700">
                {sorted.map((t) => (
                  <Fragment key={t.teacherId}>
                    <tr className="hover:bg-gray-50/80 transition-colors">
                      <td className="px-6 py-4 font-medium text-gray-900">
                        <div>{t.fullName || t.teacherId}</div>
                        {t.email && (
                          <div className="text-xs text-gray-400 font-normal">{t.email}</div>
                        )}
                      </td>
                      <td className="px-6 py-4">
                        <StatusPill status={t.verificationStatus} />
                      </td>
                      <td className="px-6 py-4">{t.totalUploads || 0}</td>
                      <td className="px-6 py-4">
                        {t.engagementScore ? t.engagementScore.toFixed(1) : "0.0"}
                      </td>
                      <td className="px-6 py-4 font-mono font-semibold text-gray-900">
                        {(t.earningsBalanceTzs || 0).toLocaleString()} TZS
                      </td>
                      <td className="px-6 py-4 text-right">
                        {t.verificationStatus === "PENDING" && (
                          <div className="flex items-center justify-end gap-2">
                            <button
                              className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg font-medium text-xs transition disabled:opacity-50"
                              disabled={busyId === t.teacherId}
                              onClick={() => approve(t.teacherId)}
                            >
                              {busyId === t.teacherId ? "Inasindika..." : "Thibitisha"}
                            </button>
                            <button
                              className="px-3 py-1.5 bg-rose-50 hover:bg-rose-100 text-rose-700 rounded-lg font-medium text-xs transition disabled:opacity-50"
                              disabled={busyId === t.teacherId}
                              onClick={() => setRejectingId(t.teacherId)}
                            >
                              Kataa
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>

                    {/* Rejection Form Dropdown */}
                    {rejectingId === t.teacherId && (
                      <tr className="bg-rose-50/50">
                        <td colSpan={6} className="px-6 py-4">
                          <div className="flex flex-col sm:flex-row items-center gap-3">
                            <input
                              className="flex-1 w-full px-4 py-2 rounded-xl border border-rose-200 text-sm outline-none focus:ring-2 focus:ring-rose-500 bg-white"
                              placeholder="Andika sababu ya kukataa (itaonekana kwa mwalimu)..."
                              value={rejectNotes}
                              onChange={(e) => setRejectNotes(e.target.value)}
                            />
                            <div className="flex items-center gap-2 w-full sm:w-auto justify-end">
                              <button
                                className="px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-semibold transition disabled:opacity-50"
                                disabled={busyId === t.teacherId}
                                onClick={() => submitReject(t.teacherId)}
                              >
                                Thibitisha Kukataa
                              </button>
                              <button
                                className="px-4 py-2 bg-gray-200 hover:bg-gray-300 text-gray-700 rounded-xl text-xs font-semibold transition"
                                onClick={() => {
                                  setRejectingId(null);
                                  setRejectNotes("");
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
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
