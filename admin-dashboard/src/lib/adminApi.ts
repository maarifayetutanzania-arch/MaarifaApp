import { doc, updateDoc } from "firebase/firestore";
import { db } from "../firebase";

export const adminApi = {
  // --- TEACHERS MANAGEMENT ---
  approveTeacher: async (teacherId: string) => {
    const teacherRef = doc(db, "teachers", teacherId);
    await updateDoc(teacherRef, {
      verificationStatus: "APPROVED",
      updatedAt: new Date().toISOString()
    });
    return { success: true };
  },

  rejectTeacher: async (teacherId: string, notes: string) => {
    const teacherRef = doc(db, "teachers", teacherId);
    await updateDoc(teacherRef, {
      verificationStatus: "REJECTED",
      rejectionReason: notes,
      updatedAt: new Date().toISOString()
    });
    return { success: true };
  },

  // --- CONTENT & MATERIALS MANAGEMENT ---
  approveMaterial: async (materialId: string) => {
    const materialRef = doc(db, "materials", materialId);
    await updateDoc(materialRef, {
      status: "APPROVED",
      updatedAt: new Date().toISOString()
    });
    return { success: true };
  },

  rejectMaterial: async (materialId: string, reason: string) => {
    const materialRef = doc(db, "materials", materialId);
    await updateDoc(materialRef, {
      status: "REJECTED",
      rejectionReason: reason,
      updatedAt: new Date().toISOString()
    });
    return { success: true };
  },

  // --- PAYOUTS & TRANSACTIONS MANAGEMENT ---
  approvePayout: async (payoutId: string) => {
    const payoutRef = doc(db, "payouts", payoutId);
    await updateDoc(payoutRef, {
      status: "APPROVED",
      updatedAt: new Date().toISOString()
    });
    return { success: true };
  },

  markPayoutPaid: async (payoutId: string, transactionId: string) => {
    const payoutRef = doc(db, "payouts", payoutId);
    await updateDoc(payoutRef, {
      status: "PAID",
      transactionId: transactionId,
      paidAt: new Date().toISOString()
    });
    return { success: true };
  },

  flagPayoutException: async (payoutId: string, notes: string) => {
    const payoutRef = doc(db, "payouts", payoutId);
    await updateDoc(payoutRef, {
      status: "EXCEPTION",
      exceptionNotes: notes,
      updatedAt: new Date().toISOString()
    });
    return { success: true };
  },
};
