import { collection, orderBy, query, limit } from "firebase/firestore";
import { db } from "../firebase";
import { useCollection } from "../lib/useCollection";
import { StatusPill, EmptyState, formatDate, formatTzs } from "../components/Common";
import { Subscription } from "../types";

export function SubscriptionsPage() {
  const { data: subs, loading } = useCollection<Subscription>(query(collection(db, "subscriptions"), orderBy("createdAt", "desc"), limit(200)));

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
      ) : subs.length === 0 ? (
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
            {subs.map((s) => (
              <tr key={s.subscriptionId}>
                <td>{s.userId}</td>
                <td>{s.planType}</td>
                <td>{formatTzs(s.amountTzs)}</td>
                <td>{s.channel?.replace("_", " ")}</td>
                <td style={{ fontFamily: "monospace", fontSize: "0.82rem" }}>{s.transactionId || "—"}</td>
                <td><StatusPill status={s.status} /></td>
                <td>{formatDate(s.startDate)} – {formatDate(s.endDate)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
