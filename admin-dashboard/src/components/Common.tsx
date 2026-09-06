import { ReactNode } from "react";

// STATUS_MAP yenye rangi zilizosawazishwa kwa Dark Theme / Light Theme
const STATUS_MAP: Record<string, { cls: string; label: string }> = {
  PENDING_REVIEW: { cls: "bg-amber-500/10 text-amber-400 border-amber-500/20", label: "Pending review" },
  PENDING: { cls: "bg-amber-500/10 text-amber-400 border-amber-500/20", label: "Pending" },
  GENERATED: { cls: "bg-amber-500/10 text-amber-400 border-amber-500/20", label: "Generated" },
  UNDER_REVIEW: { cls: "bg-amber-500/10 text-amber-400 border-amber-500/20", label: "Under review" },
  APPROVED: { cls: "bg-emerald-500/10 text-emerald-400 border-emerald-500/20", label: "Approved" },
  VERIFIED: { cls: "bg-emerald-500/10 text-emerald-400 border-emerald-500/20", label: "Verified" },
  ACTIVE: { cls: "bg-emerald-500/10 text-emerald-400 border-emerald-500/20", label: "Active" },
  PAID: { cls: "bg-emerald-500/10 text-emerald-400 border-emerald-500/20", label: "Paid" },
  REJECTED: { cls: "bg-rose-500/10 text-rose-400 border-rose-500/20", label: "Rejected" },
  FAILED: { cls: "bg-rose-500/10 text-rose-400 border-rose-500/20", label: "Failed" },
  EXCEPTION: { cls: "bg-rose-500/10 text-rose-400 border-rose-500/20", label: "Exception" },
  EXPIRED: { cls: "bg-slate-800 text-slate-400 border-slate-700", label: "Expired" },
  CANCELLED: { cls: "bg-slate-800 text-slate-400 border-slate-700", label: "Cancelled" },
  PENDING_PAYMENT: { cls: "bg-sky-500/10 text-sky-400 border-sky-500/20", label: "Awaiting payment" },
};

export function StatusPill({ status }: { status?: string }) {
  const key = status?.toUpperCase() ?? "";
  const entry = STATUS_MAP[key] ?? {
    cls: "bg-slate-800 text-slate-400 border-slate-700",
    label: status || "Unknown",
  };

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border shrink-0 ${entry.cls}`}
    >
      {entry.label}
    </span>
  );
}

export function StatCard({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="bg-slate-900 p-6 rounded-2xl border border-slate-800 shadow-sm flex flex-col justify-between">
      <div className="text-2xl md:text-3xl font-bold text-slate-100 tracking-tight">
        {value ?? "—"}
      </div>
      <div className="text-xs font-medium text-slate-400 uppercase tracking-wider mt-2">
        {label}
      </div>
    </div>
  );
}

export function EmptyState({ text }: { text: string }) {
  return (
    <div className="w-full py-12 px-4 text-center bg-slate-900 rounded-2xl border border-dashed border-slate-800 text-slate-400 text-sm">
      {text}
    </div>
  );
}

export function formatTzs(amount?: number | null): string {
  const validAmount = typeof amount === "number" && !isNaN(amount) ? amount : 0;
  return new Intl.NumberFormat("en-TZ").format(validAmount) + " TZS";
}

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
