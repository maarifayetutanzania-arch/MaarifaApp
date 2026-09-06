import { ReactNode } from "react";

/**
 * StatusPill badala ya Tailwind Slate inatumia madarasa ya .pill kutoka index.css
 */
export function StatusPill({ status }: { status?: string }) {
  const key = status?.toUpperCase() ?? "";

  let pillClass = "pill-neutral";

  if (["PENDING", "PENDING_REVIEW", "GENERATED", "UNDER_REVIEW", "PENDING_PAYMENT"].includes(key)) {
    pillClass = "pill-pending";
  } else if (["APPROVED", "VERIFIED", "ACTIVE", "PAID"].includes(key)) {
    pillClass = "pill-approved";
  } else if (["REJECTED", "FAILED", "EXCEPTION"].includes(key)) {
    pillClass = "pill-rejected";
  }

  return <span className={`pill ${pillClass}`}>{status || "Unknown"}</span>;
}

/**
 * StatCard inayotumia .card na .stat-card za index.css
 */
export function StatCard({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="card stat-card">
      <div className="value">{value ?? "—"}</div>
      <div className="label">{label}</div>
    </div>
  );
}

/**
 * EmptyState inayotumia .card na .empty-state kutoka index.css
 */
export function EmptyState({ text }: { text: string }) {
  return <div className="card empty-state">{text}</div>;
}

/**
 * Helper function ya ku-format hela (TZS)
 */
export function formatTzs(amount?: number | null): string {
  const validAmount = typeof amount === "number" && !isNaN(amount) ? amount : 0;
  return new Intl.NumberFormat("en-TZ").format(validAmount) + " TZS";
}

/**
 * Helper function ya ku-format tarehe (Firestore Timestamp au JS Date)
 */
export function formatDate(
  value?: { toDate?: () => Date } | Date | number | string | null
): string {
  if (!value) return "—";

  try {
    let date: Date;

    if (
      typeof value === "object" &&
      value !== null &&
      "toDate" in value &&
      typeof value.toDate === "function"
    ) {
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
