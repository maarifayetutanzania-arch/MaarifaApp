import { httpsCallable } from "firebase/functions";
import { functions } from "../firebase";

/**
 * Every one of these just calls the matching Cloud Function in
 * functions/src/adminActions.ts, which re-verifies role === "ADMIN" server-side
 * before writing anything. This file never touches Firestore directly for writes —
 * firestore.rules blocks that outright for teachers/subscriptions/payouts.
 */
export const adminApi = {
  approveTeacher: (teacherId: string) => httpsCallable(functions, "adminApproveTeacher")({ teacherId }),
  rejectTeacher: (teacherId: string, notes: string) => httpsCallable(functions, "adminRejectTeacher")({ teacherId, notes }),

  approveMaterial: (materialId: string) => httpsCallable(functions, "adminApproveMaterial")({ materialId }),
  rejectMaterial: (materialId: string, reason: string) => httpsCallable(functions, "adminRejectMaterial")({ materialId, reason }),

  approvePayout: (payoutId: string) => httpsCallable(functions, "adminApprovePayout")({ payoutId }),
  markPayoutPaid: (payoutId: string, transactionId: string) =>
    httpsCallable(functions, "adminMarkPayoutPaid")({ payoutId, transactionId }),
  flagPayoutException: (payoutId: string, notes: string) =>
    httpsCallable(functions, "adminFlagPayoutException")({ payoutId, notes }),
};
