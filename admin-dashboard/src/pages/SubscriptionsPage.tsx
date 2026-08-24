import { useMemo } from "react";
import { collection, orderBy, query, limit } from "firebase/firestore";
import { db } from "../firebase";
import { useCollection } from "../lib/useCollection";
import { StatusPill, EmptyState, formatDate, formatTzs } from "../components/Common";
import { Subscription } from "../types";

// 1. Memoize au weka Query nje ya component kuzuia infinite re-render loops
const subscriptionsQuery = query(
  collection(db, "subscriptions"),
  orderBy("createdAt", "desc"),
  limit(200)
);

export function SubscriptionsPage() {
  const { data: subs = [], loading } = useCollection<Subscription>(subscriptionsQuery);

  // 2. Kinga ya uhakika ya Array validation
  const safeSubs = Array.isArray(subs) ? subs : [];

  return (
    <div className="space-y-6">
      {/* Header Section */}
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Subscriptions</h1>
        <p className="text-sm text-gray-500">
          Payment log — every subscription is verified server-side before activation.
        </p>
      </div>

      {/* Main Content Area */}
      {loading ? (
        <EmptyState text="Inapakia kumbukumbu za malipo..." />
      ) : safeSubs.length === 0 ? (
        <EmptyState text="Hakuna vifuasi (subscriptions) vilivyopatikana." />
      ) : (
        <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50/50 border-b border-gray-100 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  <th className="py-3 px-4">User</th>
                  <th className="py-3 px-4">Plan</th>
                  <th className="py-3 px-4">Amount</th>
                  <th className="py-3 px-4">Channel</th>
                  <th className="py-3 px-4">Transaction ref</th>
                  <th className="py-3 px-4">Status</th>
                  <th className="py-3 px-4">Period</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50 text-sm text-gray-700">
                {safeSubs.map((s) => (
                  <tr key={s.subscriptionId || Math.random().toString()} className="hover:bg-gray-50/50 transition-colors">
                    <td className="py-3.5 px-4 font-mono text-xs text-gray-600 truncate max-w-[140px]">
                      {s.userId || "—"}
                    </td>
                    <td className="py-3.5 px-4 font-medium text-gray-900">{s.planType || "—"}</td>
                    <td className="py-3.5 px-4 font-semibold text-gray-800">{formatTzs(s.amountTzs)}</td>
                    <td className="py-3.5 px-4 capitalize text-gray-600">
                      {s.channel ? s.channel.replace("_", " ") : "—"}
                    </td>
                    <td className="py-3.5 px-4 font-mono text-xs text-gray-500">
                      {s.transactionId || "—"}
                    </td>
                    <td className="py-3.5 px-4">
                      <StatusPill status={s.status} />
                    </td>
                    <td className="py-3.5 px-4 text-xs text-gray-500 whitespace-nowrap">
                      {formatDate(s.startDate)} – {formatDate(s.endDate)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

export default SubscriptionsPage;
