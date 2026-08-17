import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import { COLLECTIONS } from "./admin";
import { notifyUser } from "./notifications";

/**
 * Fires whenever the admin dashboard changes a material's status. Handles both the
 * teacher-facing approval/rejection notification (PRD 8.9) and the "new content"
 * notification broadcast implicitly by materials becoming visible in the library
 * (the library itself is realtime, so students just see it appear — no fan-out spam
 * notification is sent to every student for every new upload).
 */
export const onMaterialStatusChanged = onDocumentUpdated(`${COLLECTIONS.MATERIALS}/{materialId}`, async (event) => {
  const before = event.data?.before?.data();
  const after = event.data?.after?.data();
  if (!before || !after || before.status === after.status) return;

  if (after.status === "APPROVED") {
    await notifyUser(after.teacherId, "APPROVAL", "Material approved", `"${after.title}" is now live in the library.`);
  } else if (after.status === "REJECTED") {
    await notifyUser(
      after.teacherId,
      "REJECTION",
      "Material needs changes",
      after.rejectionReason ? `"${after.title}" was not approved: ${after.rejectionReason}` : `"${after.title}" was not approved.`
    );
  }
});

/** Notifies a teacher the moment the admin dashboard verifies (or rejects) their application. */
export const onTeacherVerificationChanged = onDocumentUpdated(`${COLLECTIONS.TEACHERS}/{teacherId}`, async (event) => {
  const before = event.data?.before?.data();
  const after = event.data?.after?.data();
  if (!before || !after || before.verificationStatus === after.verificationStatus) return;

  if (after.verificationStatus === "VERIFIED") {
    await notifyUser(after.userId, "APPROVAL", "You're verified!", "Your teacher account is approved — you can now upload materials.");
  } else if (after.verificationStatus === "REJECTED") {
    await notifyUser(after.userId, "REJECTION", "Application not approved", after.verificationNotes || "Contact support for details.");
  }
});
