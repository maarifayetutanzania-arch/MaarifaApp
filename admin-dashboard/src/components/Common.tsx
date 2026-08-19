import { ReactNode } from "react";

// 1. Ilundwe mara moja tu nje ya render loop
const STATUS_MAP: Record<string, { cls: string; label: string }> = {
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

export function StatusPill({ status }: { status?: string }) {
  const key = status?.toUpperCase() ?? "";
  const entry = STATUS_MAP[key] ?? { cls: "pill-neutral", label: status || "Unknown" };
  return <span className={`pill ${entry.cls}`}>{entry.label}</span>;
}

export function StatCard({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="card stat-card">
      <div className="value">{value ?? "—"}</div>
      <div className="label">{label}</div>
    </div>
  );
}

export function EmptyState({ text }: { text: string }) {
  return <div className="empty-state">{text}</div>;
}

export function formatTzs(amount?: number | null): string {
  const validAmount = typeof amount === "number" && !isNaN(amount) ? amount : 0;
  return new Intl.NumberFormat("en-TZ").format(validAmount) + " TZS";
}

export function formatDate(value?: { toDate?: () => Date } | Date | number | string | null): string {
  if (!value) return "—";

  try {
    let date: Date;

    if (typeof value === "object" && value !== null && "toDate" in value && typeof value.toDate === "function") {
      date = value.toDate();
    } else if (value instanceof Date) {
      date = value;
    } else {
      date = new Date(value as string | number);
    }

    if (isNaN(date.getTime())) return "—";

    return date.toLocaleDateString("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
  } catch {
    return "—";
  }
}
