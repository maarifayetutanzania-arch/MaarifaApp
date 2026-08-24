import { useMemo } from "react";
import { collection, query, where, limit, orderBy } from "firebase/firestore";
import { db } from "../firebase";
import { useCollection } from "../lib/useCollection";
import { StatCard, StatusPill, EmptyState, formatTzs, formatDate } from "../components/Common";
import { Teacher, Material, Subscription, Payout } from "../types";

// 1. Queries zote zimewekwa nje ya render loop kuzuia infinite re-renders
const pendingTeachersQuery = query(
  collection(db, "teachers"),
  where("verificationStatus", "==", "PENDING")
);

const pendingMaterialsQuery = query(
  collection(db, "materials"),
  where("status", "==", "PENDING_REVIEW")
);

const recentSubsQuery = query(
  collection(db, "subscriptions"),
  orderBy("createdAt", "desc"),
  limit(5)
);

const pendingPayoutsQuery = query(
  collection(db, "payouts"),
  where("status", "==", "GENERATED")
);

export function DashboardHome() {
  const { data: pendingTeachers = [], loading: loadingTeachers } =
    useCollection<Teacher>(pendingTeachersQuery);
  const { data: pendingMaterials = [], loading: loadingMaterials } =
    useCollection<Material>(pendingMaterialsQuery);
  const { data: recentSubs = [], loading: loadingSubs } =
    useCollection<Subscription>(recentSubsQuery);
  const { data: pendingPayouts = [], loading: loadingPayouts } =
    useCollection<Payout>(pendingPayoutsQuery);

  // Kuhesabu jumla ya kiasi cha payouts zinazosubiri
  const totalPendingPayoutAmount = useMemo(() => {
    if (!Array.isArray(pendingPayouts)) return 0;
    return pendingPayouts.reduce((acc, curr) => acc + (curr.amountTzs || 0), 0);
  }, [pendingPayouts]);

  const isLoading = loadingTeachers || loadingMaterials || loadingSubs || loadingPayouts;

  return (
    <div className="space-y-8">
      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Overview</h1>
        <p className="text-sm text-gray-500 mt-1">
          Muhtasari wa shughuli za mfumo wa Maarifa na maombi yanayosubiri idhini.
        </p>
      </div>

      {/* Quick Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          label="Pending Teachers"
          value={loadingTeachers ? "…" : pendingTeachers.length}
        />
        <StatCard
          label="Pending Materials"
          value={loadingMaterials ? "…" : pendingMaterials.length}
        />
        <StatCard
          label="Pending Payouts"
          value={loadingPayouts ? "…" : pendingPayouts.length}
        />
        <StatCard
          label="Pending Payout TZS"
          value={loadingPayouts ? "…" : formatTzs(totalPendingPayoutAmount)}
        />
      </div>

      {/* Main Content Sections */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Recent Subscriptions (2 Columns) */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-bold text-gray-900">Recent Subscriptions</h2>
            <span className="text-xs text-gray-400 font-medium">Last 5 transactions</span>
          </div>

          {isLoading ? (
            <EmptyState text="Inapakia takwimu..." />
          ) : recentSubs.length === 0 ? (
            <EmptyState text="Hakuna vifuasi (subscriptions) vilivyolipwa hivi karibuni." />
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-gray-100 text-xs text-gray-400 uppercase tracking-wider">
                    <th className="pb-3 font-semibold">User ID</th>
                    <th className="pb-3 font-semibold">Plan</th>
                    <th className="pb-3 font-semibold">Amount</th>
                    <th className="pb-3 font-semibold">Status</th>
                    <th className="pb-3 font-semibold">Date</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50 text-sm">
                  {recentSubs.map((sub) => (
                    <tr key={sub.subscriptionId || Math.random().toString()} className="hover:bg-gray-50/50">
                      <td className="py-3 font-mono text-xs text-gray-600 truncate max-w-[120px]">
                        {sub.userId || "—"}
                      </td>
                      <td className="py-3 font-medium text-gray-800">{sub.planType || "—"}</td>
                      <td className="py-3 text-gray-700">{formatTzs(sub.amountTzs)}</td>
                      <td className="py-3">
                        <StatusPill status={sub.status} />
                      </td>
                      <td className="py-3 text-xs text-gray-500">{formatDate(sub.createdAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Action Items Summary (1 Column) */}
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-4">
          <h2 className="text-lg font-bold text-gray-900">Pending Actions</h2>

          <div className="space-y-3">
            <div className="p-4 rounded-xl bg-amber-50/60 border border-amber-100 flex items-center justify-between">
              <div>
                <p className="text-xs font-semibold text-amber-800">Walimu Wapya</p>
                <p className="text-sm font-bold text-amber-950 mt-0.5">
                  {pendingTeachers.length} Wanadhaniwa
                </p>
              </div>
              <span className="w-2.5 h-2.5 rounded-full bg-amber-500 animate-pulse"></span>
            </div>

            <div className="p-4 rounded-xl bg-blue-50/60 border border-blue-100 flex items-center justify-between">
              <div>
                <p className="text-xs font-semibold text-blue-800">Nyenzo (Materials)</p>
                <p className="text-sm font-bold text-blue-950 mt-0.5">
                  {pendingMaterials.length} Zinahitaji Uhakiki
                </p>
              </div>
              <span className="w-2.5 h-2.5 rounded-full bg-blue-500 animate-pulse"></span>
            </div>

            <div className="p-4 rounded-xl bg-emerald-50/60 border border-emerald-100 flex items-center justify-between">
              <div>
                <p className="text-xs font-semibold text-emerald-800">Maombi ya Malipo</p>
                <p className="text-sm font-bold text-emerald-950 mt-0.5">
                  {pendingPayouts.length} Payouts
                </p>
              </div>
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse"></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default DashboardHome;
