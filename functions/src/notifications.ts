import { db, messaging, COLLECTIONS, FieldValue } from "./admin";

export async function notifyUser(
  userId: string,
  category: string,
  title: string,
  body: string,
  relatedId = ""
): Promise<void> {
  await db.collection(COLLECTIONS.NOTIFICATIONS).add({
    userId,
    category,
    title,
    body,
    relatedId,
    read: false,
    createdAt: FieldValue.serverTimestamp(),
  });

  const userSnap = await db.collection(COLLECTIONS.USERS).doc(userId).get();
  const token = userSnap.data()?.fcmToken as string | undefined;
  if (!token) return;

  try {
    await messaging.send({
      token,
      notification: { title, body },
      data: { category, relatedId },
    });
  } catch (err) {
    // A stale/invalid token should never fail the calling function's main job
    // (e.g. payment verification) — log and move on.
    console.warn(`FCM send failed for user ${userId}:`, err);
  }
}
