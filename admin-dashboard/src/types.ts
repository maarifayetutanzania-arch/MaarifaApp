export interface AppUser {
  userId: string;
  fullName: string;
  phoneNumber: string;
  email: string;
  role: "STUDENT" | "TEACHER" | "ADMIN" | "UNKNOWN";
  region: string;
  schoolName?: string | null;
  formClass: string;
  status: string;
  createdAt?: { toDate: () => Date } | null;
}

export interface Teacher {
  teacherId: string;
  userId: string;
  verificationStatus: "PENDING" | "VERIFIED" | "REJECTED";
  verificationNotes: string;
  totalUploads: number;
  totalReaders: number;
  engagementScore: number;
  earningsBalanceTzs: number;
  payoutStatus: string;
}

export interface Material {
  materialId: string;
  teacherId: string;
  teacherName: string;
  title: string;
  description: string;
  form: string;
  subject: string;
  topic: string;
  fileUrl: string;
  status: "PENDING_REVIEW" | "APPROVED" | "REJECTED";
  rejectionReason: string;
  uniqueReaderCount: number;
  totalReadCount: number;
  createdAt?: { toDate: () => Date } | null;
}

export interface Subscription {
  subscriptionId: string;
  userId: string;
  planType: string;
  amountTzs: number;
  channel: string;
  status: "PENDING_PAYMENT" | "ACTIVE" | "EXPIRED" | "FAILED" | "CANCELLED";
  transactionId: string;
  startDate?: { toDate: () => Date } | null;
  endDate?: { toDate: () => Date } | null;
  createdAt?: { toDate: () => Date } | null;
}

export interface Payout {
  payoutId: string;
  teacherId: string;
  teacherName: string;
  period: string;
  engagementSharePercent: number;
  calculatedAmountTzs: number;
  status: "GENERATED" | "UNDER_REVIEW" | "APPROVED" | "PAID" | "EXCEPTION";
  approvedBy: string;
  transactionId: string;
  createdAt?: { toDate: () => Date } | null;
}
