import { httpsCallable } from "firebase/functions";
import { functions } from "./firebase"; // au path sahihi ya firebase config yako

export const adminApi = {
  // --- TEACHERS MANAGEMENT ---
  approveTeacher: async (teacherId: string) => {
    const adminApproveTeacherFn = httpsCallable(functions, "adminApproveTeacher");
    const result = await adminApproveTeacherFn({ teacherId });
    return result.data as { success: boolean; message?: string };
  },

  rejectTeacher: async (teacherId: string, notes: string) => {
    const adminRejectTeacherFn = httpsCallable(functions, "adminRejectTeacher");
    const result = await adminRejectTeacherFn({ teacherId, notes });
    return result.data as { success: boolean; message?: string };
  },

  // --- CONTENT & MATERIALS MANAGEMENT ---
  approveMaterial: async (materialId: string) => {
    const adminApproveMaterialFn = httpsCallable(functions, "adminApproveMaterial");
    const result = await adminApproveMaterialFn({ materialId });
    return result.data as { success: boolean; message?: string };
  },

  rejectMaterial: async (materialId: string, reason: string) => {
    const adminRejectMaterialFn = httpsCallable(functions, "adminRejectMaterial");
    const result = await adminRejectMaterialFn({ materialId, reason });
    return result.data as { success: boolean; message?: string };
  },

  // --- PAYOUTS & TRANSACTIONS MANAGEMENT ---
  approvePayout: async (payoutId: string) => {
    const adminApprovePayoutFn = httpsCallable(functions, "adminApprovePayout");
    const result = await adminApprovePayoutFn({ payoutId });
    return result.data as { success: boolean; message?: string };
  },

  markPayoutPaid: async (payoutId: string, transactionId: string) => {
    const adminMarkPayoutPaidFn = httpsCallable(functions, "adminMarkPayoutPaid");
    const result = await adminMarkPayoutPaidFn({ payoutId, transactionId });
    return result.data as { success: boolean; message?: string };
  },

  flagPayoutException: async (payoutId: string, notes: string) => {
    const adminFlagPayoutExceptionFn = httpsCallable(functions, "adminFlagPayoutException");
    const result = await adminFlagPayoutExceptionFn({ payoutId, notes });
    return result.data as { success: boolean; message?: string };
  },
};
