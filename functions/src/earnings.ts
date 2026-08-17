import { onSchedule } from "firebase-functions/v2/scheduler";
import { db, COLLECTIONS, FieldValue, TEACHER_POOL_SHARE } from "./admin";
import { recomputeAllEngagementScores } from "./engagement";
import { notifyUser } from "./notifications";

function periodKey(date: Date): string {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
}

/**
 * PRD 8.8 / 14, run exactly once per period (monthly, via the schedule below):
 *   1. Sum verified subscription revenue for the period.
 *   2. Recompute every teacher's engagement score.
 *   3. Split 75% of revenue across teachers proportional to their score share.
 *   4. Write one Payout doc per teacher with status GENERATED for admin review,
 *      and roll the amount into teachers/{id}.earningsBalanceTzs.
 *
 * The admin dashboard reviews/approves these — it never computes the split itself.
 */
export async function runEarningsAndPayouts(forDate: Date = new Date()): Promise<void> {
  const period = periodKey(forDate);
  const periodStart = new Date(forDate.getFullYear(), forDate.getMonth(), 1);
  const periodEnd = new Date(forDate.getFullYear(), forDate.getMonth() + 1, 1);

  const verifiedSnap = await db
    .collection(COLLECTIONS.SUBSCRIPTIONS)
    .where("status", "==", "ACTIVE")
    .where("verifiedAt", ">=", periodStart)
    .where("verifiedAt", "<", periodEnd)
    .get();

  const periodRevenueTzs = verifiedSnap.docs.reduce((sum, d) => sum + (d.data().amountTzs as number), 0);
  if (periodRevenueTzs <= 0) {
    console.log(`No verified revenue for period ${period}; skipping payout generation.`);
    return;
  }

  const teacherScores = await recomputeAllEngagementScores();
  const totalScore = Array.from(teacherScores.values()).reduce((a, b) => a + b, 0);
  if (totalScore <= 0) {
    console.log(`No teacher engagement recorded for period ${period}; skipping payout generation.`);
    return;
  }

  const teacherPoolTzs = Math.round(periodRevenueTzs * TEACHER_POOL_SHARE);

  const batch = db.batch();
  for (const [teacherId, score] of teacherScores.entries()) {
    if (score <= 0) continue;
    const share = score / totalScore;
    const amountTzs = Math.round(teacherPoolTzs * share);
    if (amountTzs <= 0) continue;

    const teacherSnap = await db.collection(COLLECTIONS.TEACHERS).doc(teacherId).get();
    const teacherName = teacherSnap.exists ? ((await db.collection(COLLECTIONS.USERS).doc(teacherId).get()).data()?.fullName ?? "") : "";

    const payoutRef = db.collection(COLLECTIONS.PAYOUTS).doc();
    batch.set(payoutRef, {
      payoutId: payoutRef.id,
      teacherId,
      teacherName,
      period,
      engagementSharePercent: share * 100,
      calculatedAmountTzs: amountTzs,
      status: "GENERATED",
      approvedBy: "",
      transactionId: "",
      createdAt: FieldValue.serverTimestamp(),
    });
    batch.update(db.collection(COLLECTIONS.TEACHERS).doc(teacherId), {
      earningsBalanceTzs: FieldValue.increment(amountTzs),
      payoutStatus: "GENERATED",
    });
  }
  await batch.commit();

  for (const teacherId of teacherScores.keys()) {
    if ((teacherScores.get(teacherId) ?? 0) > 0) {
      await notifyUser(teacherId, "EARNINGS", "New payout generated", `Your ${period} earnings are ready for admin review.`);
    }
  }
}

/** Runs at 02:00 on the 1st of every month (Africa/Dar_es_Salaam) for the just-finished period. */
export const generateEarningsAndPayouts = onSchedule(
  { schedule: "0 2 1 * *", timeZone: "Africa/Dar_es_Salaam" },
  async () => {
    const lastMonth = new Date();
    lastMonth.setMonth(lastMonth.getMonth() - 1);
    await runEarningsAndPayouts(lastMonth);
  }
);
