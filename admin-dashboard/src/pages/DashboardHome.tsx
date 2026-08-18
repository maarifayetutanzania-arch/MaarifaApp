import { collection, orderBy, query, limit } from "firebase/firestore";
import { db } from "../firebase";
import { useCollection } from "../lib/useCollection";
import { StatusPill, EmptyState, formatDate, formatTzs } from "../components/Common";
import { Subscription } from "../types";

// 1. Query imewkwa nje ya component kuzuia infinite re-render loop
const subscriptionsQuery = query(
  collection(db, "subscriptions"),
  orderBy("createdAt", "desc"),
  limit(200)
);

export function SubscriptionsPage() {
  const { data: subs = [], loading } = useCollection<Subscription>(subscriptionsQuery);

  // 2. Kinga ya uhakika ili kuzuia crash kama data bado haijafika
  const safeSubs = Array.isArray(subs) ? subs : [];

  return (
    <div className="main">
      <div className="page-header">
        <div>
          <h1>Subscriptions</h1>
          <p>Payment log — every subscription is verified server-side before activation.</p>
        </div>
      </div>

      {loading ? (
        <EmptyState text="Loading…" />
      ) : safeSubs.length === 0 ? (
        <EmptyState text="No subscriptions yet." />
      ) : (
        <table>
          <thead>
            <tr>
              <th>User</th>
              <th>Plan</th>
              <th>Amount</th>
              <th>Channel</th>
              <th>Transaction ref</th>
              <th>Status</th>
              <th>Period</th>
            </tr>
          </thead>
          <tbody>
            {safeSubs.map((s) => (
              <tr key={s.subscriptionId || Math.random().toString()}>
                <td>{s.userId || "N/A"}</td>
                <td>{s.planType || "N/A"}</td>
                <td>{formatTzs(s.amountTzs || 0)}</td>
                <td>{s.channel ? s.channel.replace("_", " ") : "N/A"}</td>
                <td style={{ fontFamily: "monospace", fontSize: "0.82rem" }}>
                  {s.transactionId || "—"}
                </td>
                <td><StatusPill status={s.status} /></td>
                <td>
                  {s.startDate ? formatDate(s.startDate) : "N/A"} – {s.endDate ? formatDate(s.endDate) : "N/A"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
