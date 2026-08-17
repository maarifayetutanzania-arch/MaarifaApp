import * as functions from "firebase-functions/v2/https";
import { db, COLLECTIONS, FieldValue } from "./admin";

async function assertIsAdmin(uid: string | undefined): Promise<void> {
  if (!uid) throw new functions.HttpsError("unauthenticated", "Sign in required.");
  const snap = await db.collection(COLLECTIONS.USERS).doc(uid).get();
  if (snap.data()?.role !== "ADMIN") {
    throw new functions.HttpsError("permission-denied", "Admin access required.");
  }
}

// ---------------- Teacher verification ----------------

interface TeacherDecisionRequest {
  teacherId: string;
  notes?: string;
}

export const adminApproveTeacher = functions.onCall(async (request) => {
  await assertIsAdmin(request.auth?.uid);
  const { teacherId } = request.data as TeacherDecisionRequest;
  await db.collection(COLLECTIONS.TEACHERS).doc(teacherId).update({
    verificationStatus: "VERIFIED",
    verificationNotes: "",
  });
  return { ok: true };
});

export const adminRejectTeacher = functions.onCall(async (request) => {
  await assertIsAdmin(request.auth?.uid);
  const { teacherId, notes } = request.data as TeacherDecisionRequest;
  await db.collection(COLLECTIONS.TEACHERS).doc(teacherId).update({
    verificationStatus: "REJECTED",
    verificationNotes: notes ?? "",
  });
  return { ok: true };
});

// ---------------- Content moderation ----------------

interface MaterialDecisionRequest {
  materialId: string;
  reason?: string;
}

export const adminApproveMaterial = functions.onCall(async (request) => {
  await assertIsAdmin(request.auth?.uid);
  const { materialId } = request.data as MaterialDecisionRequest;
  await db.collection(COLLECTIONS.MATERIALS).doc(materialId).update({
    status: "APPROVED",
    rejectionReason: "",
    approvedAt: FieldValue.serverTimestamp(),
  });

  // Keep the owning teacher's uploaded-count accurate for the dashboard/stat cards.
  const materialSnap = await db.collection(COLLECTIONS.MATERIALS).doc(materialId).get();
  const teacherId = materialSnap.data()?.teacherId as string | undefined;
  if (teacherId) {
    await db.collection(COLLECTIONS.TEACHERS).doc(teacherId).update({
      totalUploads: FieldValue.increment(1),
    });
  }
  return { ok: true };
});

export const adminRejectMaterial = functions.onCall(async (request) => {
  await assertIsAdmin(request.auth?.uid);
  const { materialId, reason } = request.data as MaterialDecisionRequest;
  await db.collection(COLLECTIONS.MATERIALS).doc(materialId).update({
    status: "REJECTED",
    rejectionReason: reason ?? "",
  });
  return { ok: true };
});

// ---------------- Payout review (PRD 8.8/8.10 - admin reviews/approves, never computes) ----------------

interface PayoutDecisionRequest {
  payoutId: string;
  adminUid?: string; // ignored - we use request.auth.uid instead, kept out of trust boundary
  transactionId?: string;
}

export const adminApprovePayout = functions.onCall(async (request) => {
  await assertIsAdmin(request.auth?.uid);
  const { payoutId } = request.data as PayoutDecisionRequest;
  await db.collection(COLLECTIONS.PAYOUTS).doc(payoutId).update({
    status: "APPROVED",
    approvedBy: request.auth!.uid,
  });
  return { ok: true };
});

export const adminMarkPayoutPaid = functions.onCall(async (request) => {
  await assertIsAdmin(request.auth?.uid);
  const { payoutId, transactionId } = request.data as PayoutDecisionRequest;
  const payoutRef = db.collection(COLLECTIONS.PAYOUTS).doc(payoutId);
  const payoutSnap = await payoutRef.get();
  if (!payoutSnap.exists) throw new functions.HttpsError("not-found", "Payout not found.");

  await payoutRef.update({
    status: "PAID",
    transactionId: transactionId ?? "",
  });

  const teacherId = payoutSnap.data()!.teacherId as string;
  const amount = payoutSnap.data()!.calculatedAmountTzs as number;
  await db.collection(COLLECTIONS.TEACHERS).doc(teacherId).update({
    earningsBalanceTzs: FieldValue.increment(-amount),
    payoutStatus: "PAID",
  });
  return { ok: true };
});

export const adminFlagPayoutException = functions.onCall(async (request) => {
  await assertIsAdmin(request.auth?.uid);
  const { payoutId, notes } = request.data as { payoutId: string; notes?: string };
  await db.collection(COLLECTIONS.PAYOUTS).doc(payoutId).update({
    status: "EXCEPTION",
    transactionId: notes ?? "",
  });
  return { ok: true };
});
