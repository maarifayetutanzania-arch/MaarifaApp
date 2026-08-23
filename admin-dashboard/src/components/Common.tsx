import { ReactNode } from "react";

// STATUS_MAP imewekwa nje ya render loop kuzuia re-allocation
const STATUS_MAP: Record<string, { cls: string; label: string }> = {
  PENDING_REVIEW: { cls: "bg-amber-50 text-amber-700 border-amber-200", label: "Pending review" },
  PENDING: { cls: "bg-amber-50 text-amber-700 border-amber-200", label: "Pending" },
  GENERATED: { cls: "bg-amber-50 text-amber-700 border-amber-200", label: "Generated" },
  UNDER_REVIEW: { cls: "bg-amber-50 text-amber-700 border-amber-200", label: "Under review" },
  APPROVED: { cls: "bg-emerald-50 text-emerald-700 border-emerald-200", label: "Approved" },
  VERIFIED: { cls: "bg-emerald-50 text-emerald-700 border-emerald-200", label: "Verified" },
  ACTIVE: { cls: "bg-emerald-50 text-emerald-700 border-emerald-200", label: "Active" },
  PAID: { cls: "bg-emerald-50 text-emerald-700 border-emerald-200", label: "Paid" },
  REJECTED: { cls: "bg-rose-50 text-rose-700 border-rose-200", label: "Rejected" },
  FAILED: { cls: "bg-rose-50 text-rose-700 border-rose-200", label: "Failed" },
  EXCEPTION: { cls: "bg-rose-50 text-rose-700 border-rose-200", label: "Exception" },
  EXPIRED: { cls: "bg-gray-100 text-gray-600 border-gray-200", label: "Expired" },
  CANCELLED: { cls: "bg-gray-100 text-gray-600 border-gray-200", label: "Cancelled" },
  PENDING_PAYMENT: { cls: "bg-blue-50 text-blue-700 border-blue-200", label: "Awaiting payment" },
};

export function StatusPill({ status }: { status?: string }) {
  const key = status?.toUpperCase() ?? "";
  const entry = STATUS_MAP[key] ?? {
    cls: "bg-gray-100 text-gray-600 border-gray-200",
    label: status || "Unknown",
  };

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${entry.cls}`}
    >
      {entry.label}
    </span>
  );
}

export function StatCard({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm flex flex-col justify-between">
      <div className="text-2xl md:text-3xl font-bold text-gray-900 tracking-tight">
        {value ?? "—"}
      </div>
      <div className="text-xs font-medium text-gray-500 uppercase tracking-wider mt-2">
        {label}
      </div>
    </div>
  );
}

export function EmptyState({ text }: { text: string }) {
  return (
    <div className="w-full py-12 px-4 text-center bg-white rounded-2xl border border-dashed border-gray-200 text-gray-500 text-sm">
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
