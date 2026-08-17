import { onSchedule } from "firebase-functions/v2/scheduler";
import { db, COLLECTIONS } from "./admin";
import { notifyUser } from "./notifications";

/**
 * PRD 8.4/13: "premium access is removed automatically when the subscription expires."
 * Runs daily; also sends a heads-up notification for plans expiring within 3 days.
 */
export const expireSubscriptions = onSchedule(
  { schedule: "every day 00:15", timeZone: "Africa/Dar_es_Salaam" },
  async () => {
    const now = new Date();

    const expiredSnap = await db
      .collection(COLLECTIONS.SUBSCRIPTIONS)
      .where("status", "==", "ACTIVE")
      .where("endDate", "<=", now)
      .get();

    const batch = db.batch();
    expiredSnap.forEach((doc) => batch.update(doc.ref, { status: "EXPIRED" }));
    if (!expiredSnap.empty) await batch.commit();

    for (const doc of expiredSnap.docs) {
      const sub = doc.data();
      await notifyUser(sub.userId, "SUBSCRIPTION_EXPIRY", "Your plan has expired", "Renew to keep accessing premium materials.");
    }

    const in3Days = new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000);
    const nearingSnap = await db
      .collection(COLLECTIONS.SUBSCRIPTIONS)
      .where("status", "==", "ACTIVE")
      .where("endDate", ">", now)
      .where("endDate", "<=", in3Days)
      .get();

    for (const doc of nearingSnap.docs) {
      const sub = doc.data();
      await notifyUser(
        sub.userId,
        "SUBSCRIPTION_EXPIRY",
        "Your plan expires soon",
        `Your ${sub.planType} plan expires on ${sub.endDate.toDate().toDateString()}. Renew to avoid losing access.`
      );
    }

    console.log(`Expired ${expiredSnap.size} subscriptions, warned ${nearingSnap.size} nearing expiry.`);
  }
);
