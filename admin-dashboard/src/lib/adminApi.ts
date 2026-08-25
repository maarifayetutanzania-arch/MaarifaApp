import { httpsCallable } from "firebase/functions";
import { functions } from "../firebase";

/**
 * All admin actions call server-side Cloud Functions in `functions/src/adminActions.ts`.
 * The backend re-verifies `role === "ADMIN"` before committing any state changes to Firestore.
 */
export const adminApi = {
  // --- TEACHERS MANAGEMENT ---
  approveTeacher: (teacherId: string) =>
    httpsCallable<{ teacherId: string }, { success: boolean }>(functions, "adminApproveTeacher")({ teacherId }),

  rejectTeacher: (teacherId: string, notes: string) =>
    httpsCallable<{ teacherId: string; notes: string }, { success: boolean }>(functions, "adminRejectTeacher")({ teacherId, notes }),

  // --- CONTENT & MATERIALS MANAGEMENT ---
  approveMaterial: (materialId: string) =>
    httpsCallable<{ materialId: string }, { success: boolean }>(functions, "adminApproveMaterial")({ materialId }),

  rejectMaterial: (materialId: string, reason: string) =>
    httpsCallable<{ materialId: string; reason: string }, { success: boolean }>(functions, "adminRejectMaterial")({ materialId, reason }),

  // --- PAYOUTS & TRANSACTIONS MANAGEMENT ---
  approvePayout: (payoutId: string) =>
    httpsCallable<{ payoutId: string }, { success: boolean }>(functions, "adminApprovePayout")({ payoutId }),

  markPayoutPaid: (payoutId: string, transactionId: string) =>
    httpsCallable<{ payoutId: string; transactionId: string }, { success: boolean }>(functions, "adminMarkPayoutPaid")({ payoutId, transactionId }),

  flagPayoutException: (payoutId: string, notes: string) =>
    httpsCallable<{ payoutId: string; notes: string }, { success: boolean }>(functions, "adminFlagPayoutException")({ payoutId, notes }),
};
