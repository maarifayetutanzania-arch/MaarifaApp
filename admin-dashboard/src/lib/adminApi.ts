import { httpsCallable } from "firebase/functions";
import { functions } from "../firebase";

const adminApproveTeacherFn = httpsCallable(functions, "adminApproveTeacher");
const adminRejectTeacherFn = httpsCallable(functions, "adminRejectTeacher");
const adminApproveMaterialFn = httpsCallable(functions, "adminApproveMaterial");
const adminRejectMaterialFn = httpsCallable(functions, "adminRejectMaterial");
const adminApprovePayoutFn = httpsCallable(functions, "adminApprovePayout");
const adminMarkPayoutPaidFn = httpsCallable(functions, "adminMarkPayoutPaid");
const adminFlagPayoutExceptionFn = httpsCallable(functions, "adminFlagPayoutException");

export interface ApiResponse {
  success: boolean;
  message?: string;
}

export const adminApi = {
  // --- TEACHERS MANAGEMENT ---
  approveTeacher: async (teacherId: string): Promise<ApiResponse> => {
    try {
      const result = await adminApproveTeacherFn({ teacherId, userId: teacherId });
      return result.data as ApiResponse;
    } catch (error: any) {
      console.error("Error approving teacher:", error);
      throw new Error(error.message || "Imefeli kuthibitisha mwalimu.");
    }
  },

  rejectTeacher: async (teacherId: string, notes: string): Promise<ApiResponse> => {
    try {
      const result = await adminRejectTeacherFn({ teacherId, userId: teacherId, notes, reason: notes });
      return result.data as ApiResponse;
    } catch (error: any) {
      console.error("Error rejecting teacher:", error);
      throw new Error(error.message || "Imefeli kukataa maombi ya mwalimu.");
    }
  },

  // --- CONTENT & MATERIALS MANAGEMENT ---
  approveMaterial: async (materialId: string): Promise<ApiResponse> => {
    try {
      const result = await adminApproveMaterialFn({ materialId });
      return result.data as ApiResponse;
    } catch (error: any) {
      console.error("Error approving material:", error);
      throw new Error(error.message || "Imefeli kuthibitisha maudhui.");
    }
  },

  rejectMaterial: async (materialId: string, reason: string): Promise<ApiResponse> => {
    try {
      const result = await adminRejectMaterialFn({ materialId, reason, notes: reason });
      return result.data as ApiResponse;
    } catch (error: any) {
      console.error("Error rejecting material:", error);
      throw new Error(error.message || "Imefeli kukataa maudhui.");
    }
  },

  // --- PAYOUTS & TRANSACTIONS MANAGEMENT ---
  approvePayout: async (payoutId: string): Promise<ApiResponse> => {
    try {
      const result = await adminApprovePayoutFn({ payoutId });
      return result.data as ApiResponse;
    } catch (error: any) {
      console.error("Error approving payout:", error);
      throw new Error(error.message || "Imefeli kuthibitisha malipo.");
    }
  },

  markPayoutPaid: async (payoutId: string, transactionId: string): Promise<ApiResponse> => {
    try {
      const result = await adminMarkPayoutPaidFn({ payoutId, transactionId });
      return result.data as ApiResponse;
    } catch (error: any) {
      console.error("Error marking payout paid:", error);
      throw new Error(error.message || "Imefeli kuweka kumbukumbu ya malipo.");
    }
  },

  flagPayoutException: async (payoutId: string, notes: string): Promise<ApiResponse> => {
    try {
      const result = await adminFlagPayoutExceptionFn({ payoutId, notes, reason: notes });
      return result.data as ApiResponse;
    } catch (error: any) {
      console.error("Error flagging payout exception:", error);
      throw new Error(error.message || "Imefeli kuweka flag kwenye malipo.");
    }
  },
};
