import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import { notifyUser } from "./notifications";

export const onMaterialStatusChanged = onDocumentUpdated("materials/{materialId}", async (event) => {
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

export const onTeacherVerificationChanged = onDocumentUpdated("teachers/{teacherId}", async (event) => {
  const before = event.data?.before?.data();
  const after = event.data?.after?.data();
  if (!before || !after || before.verificationStatus === after.verificationStatus) return;

  if (after.verificationStatus === "VERIFIED") {
    await notifyUser(after.userId, "APPROVAL", "You're verified!", "Your teacher account is approved — you can now upload materials.");
  } else if (after.verificationStatus === "REJECTED") {
    await notifyUser(after.userId, "REJECTION", "Application not approved", after.verificationNotes || "Contact support for details.");
  }
});
