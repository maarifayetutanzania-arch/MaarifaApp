import { Fragment, useState } from "react";
import { collection, orderBy, query } from "firebase/firestore";
import { db } from "../firebase";
import { useCollection } from "../lib/useCollection";
import { StatusPill, EmptyState, formatDate, formatTzs } from "../components/Common";
import { Payout } from "../types";
import { adminApi } from "../lib/adminApi";

export function PayoutsPage() {
  const { data: payouts, loading } = useCollection<Payout>(query(collection(db, "payouts"), orderBy("createdAt", "desc")));
  const [busyId, setBusyId] = useState<string | null>(null);
  const [payingId, setPayingId] = useState<string | null>(null);
  const [txnId, setTxnId] = useState("");

  const totalPending = payouts.filter((p) => p.status === "GENERATED" || p.status === "APPROVED").reduce((s, p) => s + p.calculatedAmountTzs, 0);

  const approve = async (payoutId: string) => {
    setBusyId(payoutId);
    try {
      await adminApi.approvePayout(payoutId);
    } finally {
      setBusyId(null);
    }
  };

  const markPaid = async (payoutId: string) => {
    setBusyId(payoutId);
    try {
      await adminApi.markPayoutPaid(payoutId, txnId);
      setPayingId(null);
      setTxnId("");
    } finally {
      setBusyId(null);
    }
  };

  const flagException = async (payoutId: string) => {
    setBusyId(payoutId);
    try {
      await adminApi.flagPayoutException(payoutId, "Flagged by admin for manual review");
    } finally {
      setBusyId(null);
    }
  };

  return (
    <div className="main">
      <div className="page-header">
        <div>
          <h1>Payouts</h1>
          <p>Generated automatically each period from verified revenue and engagement — review and release below.</p>
        </div>
      </div>

      <div className="stat-grid" style={{ marginBottom: 20 }}>
        <div className="card stat-card">
          <div className="value">{formatTzs(totalPending)}</div>
          <div className="label">Awaiting payout</div>
        </div>
      </div>

      {loading ? (
        <EmptyState text="Loading…" />
      ) : payouts.length === 0 ? (
        <EmptyState text="No payouts generated yet — these appear automatically once a monthly earnings run has revenue and engagement to distribute." />
      ) : (
        <table>
          <thead>
            <tr>
              <th>Teacher</th>
              <th>Period</th>
              <th>Share</th>
              <th>Amount</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {payouts.map((p) => (
              <Fragment key={p.payoutId}>
                <tr>
                  <td>{p.teacherName || p.teacherId}</td>
                  <td>{p.period}</td>
                  <td>{p.engagementSharePercent.toFixed(1)}%</td>
                  <td>{formatTzs(p.calculatedAmountTzs)}</td>
                  <td><StatusPill status={p.status} /></td>
                  <td>
                    <div className="row-actions">
                      {p.status === "GENERATED" && (
                        <button className="btn-primary" disabled={busyId === p.payoutId} onClick={() => approve(p.payoutId)}>
                          Approve
                        </button>
                      )}
                      {p.status === "APPROVED" && (
                        <button className="btn-primary" disabled={busyId === p.payoutId} onClick={() => setPayingId(p.payoutId)}>
                          Mark paid
                        </button>
                      )}
                      {(p.status === "GENERATED" || p.status === "APPROVED") && (
                        <button className="btn-danger" disabled={busyId === p.payoutId} onClick={() => flagException(p.payoutId)}>
                          Flag
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
                {payingId === p.payoutId && (
                  <tr>
                    <td colSpan={6}>
                      <div style={{ display: "flex", gap: 8 }}>
                        <input placeholder="Bank/mobile-money transaction reference" value={txnId} onChange={(e) => setTxnId(e.target.value)} />
                        <button className="btn-primary" onClick={() => markPaid(p.payoutId)}>Confirm paid</button>
                        <button className="btn-ghost" onClick={() => setPayingId(null)}>Cancel</button>
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
