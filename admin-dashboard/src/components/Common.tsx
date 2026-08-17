export function StatusPill({ status }: { status: string }) {
  const map: Record<string, { cls: string; label: string }> = {
    PENDING_REVIEW: { cls: "pill-pending", label: "Pending review" },
    PENDING: { cls: "pill-pending", label: "Pending" },
    GENERATED: { cls: "pill-pending", label: "Generated" },
    UNDER_REVIEW: { cls: "pill-pending", label: "Under review" },
    APPROVED: { cls: "pill-approved", label: "Approved" },
    VERIFIED: { cls: "pill-approved", label: "Verified" },
    ACTIVE: { cls: "pill-approved", label: "Active" },
    PAID: { cls: "pill-approved", label: "Paid" },
    REJECTED: { cls: "pill-rejected", label: "Rejected" },
    FAILED: { cls: "pill-rejected", label: "Failed" },
    EXCEPTION: { cls: "pill-rejected", label: "Exception" },
    EXPIRED: { cls: "pill-neutral", label: "Expired" },
    CANCELLED: { cls: "pill-neutral", label: "Cancelled" },
    PENDING_PAYMENT: { cls: "pill-neutral", label: "Awaiting payment" },
  };
  const entry = map[status] ?? { cls: "pill-neutral", label: status };
  return <span className={`pill ${entry.cls}`}>{entry.label}</span>;
}

export function StatCard({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="card stat-card">
      <div className="value">{value}</div>
      <div className="label">{label}</div>
    </div>
  );
}

export function EmptyState({ text }: { text: string }) {
  return <div className="empty-state">{text}</div>;
}

export function formatTzs(amount: number): string {
  return new Intl.NumberFormat("en-TZ").format(amount) + " TZS";
}

export function formatDate(value?: { toDate: () => Date } | null): string {
  if (!value) return "—";
  return value.toDate().toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric" });
}
