import { Fragment, useState, useMemo } from "react";
import { collection } from "firebase/firestore";
import { db } from "../firebase";
import { useCollection } from "../lib/useCollection";
import { StatusPill, EmptyState } from "../components/Common";
import { adminApi } from "../lib/adminApi";

export function TeachersPage() {
  // 1. Query Collection ya 'teachers' kama chanzo kikuu cha maombi ya Verification
  const teachersQuery = useMemo(() => {
    return collection(db, "teachers");
  }, []);

  // 2. Query Collection ya 'users' ili kuvuta majina na mawasiliano ya walimu
  const usersQuery = useMemo(() => {
    return collection(db, "users");
  }, []);

  const { data: teachersData, loading: loadingTeachers, error: errorTeachers } = useCollection<any>(teachersQuery);
  const { data: usersData, loading: loadingUsers } = useCollection<any>(usersQuery);

  const [busyId, setBusyId] = useState<string | null>(null);
  const [rejectingId, setRejectingId] = useState<string | null>(null);
  const [rejectNotes, setRejectNotes] = useState("");

  // Unganisha taarifa za 'teachers' na 'users'
  const mergedTeachers = useMemo(() => {
    if (!teachersData) return [];

    return teachersData.map((t) => {
      const targetUserId = t.userId || t.id;
      const userProfile = (usersData || []).find((u) => u.id === targetUserId) || {};

      const rawVerificationStatus = String(t.verificationStatus || t.status || "PENDING").trim().toUpperCase();

      return {
        ...t,
        id: t.id, // Primary key ya teacher document
        userId: targetUserId,
        fullName: userProfile.fullName || t.fullName || userProfile.email || t.id,
        email: userProfile.email || t.email || "",
        phoneNumber: userProfile.phoneNumber || t.phoneNumber || "",
        verificationStatus: rawVerificationStatus,
      };
    });
  }, [teachersData, usersData]);

  const approve = async (teacherId: string) => {
    setBusyId(teacherId);
    try {
      await adminApi.approveTeacher(teacherId);
      alert("Mwalimu amethibitishwa kikamilifu!");
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
      alert("Maombi ya mwalimu yamekataliwa.");
    } catch (err: any) {
      alert("Imefeli kukataa maombi: " + (err.message || err));
    } finally {
      setBusyId(null);
    }
  };

  // Panga ili walimu wenye maombi ya PENDING waonekane mwanzo kabisa
  const sorted = [...mergedTeachers].sort((a, b) => {
    const statusA = String(a.verificationStatus || "").toUpperCase();
    const statusB = String(b.verificationStatus || "").toUpperCase();
    return statusA === "PENDING" ? -1 : statusB === "PENDING" ? 1 : 0;
  });

  if (errorTeachers) {
    return (
      <div className="p-4 bg-rose-50 text-rose-700 rounded-xl border border-rose-200">
        <p className="font-bold">Kosa la Kupokea Data (Firestore Error):</p>
        <p className="text-sm mt-1">{errorTeachers}</p>
      </div>
    );
  }

  const isLoading = loadingTeachers || loadingUsers;

  return (
    <div className="space-y-6 font-sans">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-gray-200 pb-5">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Usimamizi wa Walimu (Verification)</h1>
          <p className="text-sm text-gray-500 mt-1">
            Kagua maombi ya walimu waliopo kwenye mchakato wa kuhakikiwa (Verification).
          </p>
        </div>
      </div>

      {isLoading ? (
        <EmptyState text="Inapakia walimu..." />
      ) : sorted.length === 0 ? (
        <EmptyState text="Hakuna maombi ya walimu yaliyopatikana kwenye collection ya 'teachers'." />
      ) : (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-200 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  <th className="px-6 py-4">Mwalimu</th>
                  <th className="px-6 py-4">Hali ya Uhakiki (Verification)</th>
                  <th className="px-6 py-4">Maudhui (Uploads)</th>
                  <th className="px-6 py-4">Engagement Score</th>
                  <th className="px-6 py-4">Salio (Balance)</th>
                  <th className="px-6 py-4 text-right">Vitendo</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 text-sm text-gray-700">
                {sorted.map((t) => {
                  const targetId = t.id;
                  const currentStatus = String(t.verificationStatus || "PENDING").toUpperCase();
                  const isPending = currentStatus === "PENDING";

                  return (
                    <Fragment key={targetId}>
                      <tr className="hover:bg-gray-50/80 transition-colors">
                        <td className="px-6 py-4 font-medium text-gray-900">
                          <div>{t.fullName}</div>
                          {t.email && (
                            <div className="text-xs text-gray-400 font-normal">{t.email}</div>
                          )}
                          {t.phoneNumber && (
                            <div className="text-xs text-gray-400 font-normal">{t.phoneNumber}</div>
                          )}
                        </td>
                        <td className="px-6 py-4">
                          <StatusPill status={currentStatus} />
                        </td>
                        <td className="px-6 py-4">{t.totalUploads || 0}</td>
                        <td className="px-6 py-4">
                          {t.engagementScore ? t.engagementScore.toFixed(1) : "0.0"}
                        </td>
                        <td className="px-6 py-4 font-mono font-semibold text-gray-900">
                          {(t.earningsBalanceTzs || 0).toLocaleString()} TZS
                        </td>
                        <td className="px-6 py-4 text-right">
                          {isPending ? (
                            <div className="flex items-center justify-end gap-2">
                              <button
                                className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg font-medium text-xs transition disabled:opacity-50"
                                disabled={busyId === targetId}
                                onClick={() => approve(targetId)}
                              >
                                {busyId === targetId ? "Inasindika..." : "Thibitisha"}
                              </button>
                              <button
                                className="px-3 py-1.5 bg-rose-50 hover:bg-rose-100 text-rose-700 rounded-lg font-medium text-xs transition disabled:opacity-50"
                                disabled={busyId === targetId}
                                onClick={() => setRejectingId(targetId)}
                              >
                                Kataa
                              </button>
                            </div>
                          ) : (
                            <div className="flex items-center justify-end gap-2">
                              <span className="text-xs text-emerald-600 font-semibold bg-emerald-50 px-2.5 py-1 rounded-md border border-emerald-200">
                                ✓ {currentStatus}
                              </span>
                              <button
                                className="px-2 py-1 bg-gray-100 hover:bg-rose-100 hover:text-rose-700 text-gray-600 rounded-lg font-medium text-xs transition"
                                onClick={() => setRejectingId(targetId)}
                              >
                                Badili
                              </button>
                            </div>
                          )}
                        </td>
                      </tr>

                      {rejectingId === targetId && (
                        <tr className="bg-rose-50/50">
                          <td colSpan={6} className="px-6 py-4">
                            <div className="flex flex-col sm:flex-row items-center gap-3">
                              <input
                                className="flex-1 w-full px-4 py-2 rounded-xl border border-rose-200 text-sm outline-none focus:ring-2 focus:ring-rose-500 bg-white"
                                placeholder="Andika sababu ya kukataa/kubadili status..."
                                value={rejectNotes}
                                onChange={(e) => setRejectNotes(e.target.value)}
                              />
                              <div className="flex items-center gap-2 w-full sm:w-auto justify-end">
                                <button
                                  className="px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-semibold transition disabled:opacity-50"
                                  disabled={busyId === targetId}
                                  onClick={() => submitReject(targetId)}
                                >
                                  Thibitisha
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
