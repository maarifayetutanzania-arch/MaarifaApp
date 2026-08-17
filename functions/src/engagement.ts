import { db, COLLECTIONS } from "./admin";

const UNIQUE_READER_WEIGHT = 6.0;
const DEPTH_WEIGHT = 8.0;
const COMPLETION_WEIGHT = 20.0;
const MAX_COUNTED_READS_PER_USER = 5;

interface EngagementRecord {
  userId: string;
  materialId: string;
  teacherId: string;
  readCount: number;
  progressPercent: number;
}

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max);
}

/**
 * Authoritative version of app/.../domain/EngagementScoreCalculator.kt — keep the two in
 * sync if you tune the weights. This copy is what actually determines real money; the
 * Kotlin copy is only used client-side to preview a teacher's live score.
 */
export function scoreForMaterial(records: EngagementRecord[]): number {
  if (records.length === 0) return 0;

  const uniqueReaders = new Set(records.map((r) => r.userId)).size;
  const avgProgress = records.reduce((sum, r) => sum + clamp(r.progressPercent, 0, 100), 0) / records.length;

  const byUser = new Map<string, number>();
  for (const r of records) {
    byUser.set(r.userId, (byUser.get(r.userId) ?? 0) + r.readCount);
  }
  let cappedReads = 0;
  byUser.forEach((count) => {
    cappedReads += Math.min(count, MAX_COUNTED_READS_PER_USER);
  });

  const readerBreadth = uniqueReaders * UNIQUE_READER_WEIGHT;
  const depthBonus = Math.log(1 + cappedReads) * DEPTH_WEIGHT;
  const completionBonus = (avgProgress / 100) * COMPLETION_WEIGHT;

  return clamp(readerBreadth + depthBonus + completionBonus, 0, 100);
}

/**
 * Recomputes every material's engagement score and rolls each teacher's materials up
 * into teachers/{id}.engagementScore + totalReaders. Called by the daily scheduled job
 * (index.ts) and can also be invoked directly by generateEarningsAndPayouts before a run.
 */
export async function recomputeAllEngagementScores(): Promise<Map<string, number>> {
  const engagementSnap = await db.collection(COLLECTIONS.ENGAGEMENT).get();
  const byMaterial = new Map<string, EngagementRecord[]>();
  engagementSnap.forEach((doc) => {
    const data = doc.data() as EngagementRecord;
    const list = byMaterial.get(data.materialId) ?? [];
    list.push(data);
    byMaterial.set(data.materialId, list);
  });

  const materialsSnap = await db.collection(COLLECTIONS.MATERIALS).get();
  const teacherMaterialScores = new Map<string, number[]>();
  const teacherReaderCounts = new Map<string, number>();

  const batch = db.batch();
  materialsSnap.forEach((doc) => {
    const material = doc.data();
    const records = byMaterial.get(doc.id) ?? [];
    const score = scoreForMaterial(records);
    const uniqueReaders = new Set(records.map((r) => r.userId)).size;
    const totalReads = records.reduce((s, r) => s + r.readCount, 0);

    batch.update(doc.ref, { uniqueReaderCount: uniqueReaders, totalReadCount: totalReads });

    const teacherId = material.teacherId as string;
    teacherMaterialScores.set(teacherId, [...(teacherMaterialScores.get(teacherId) ?? []), score]);
    teacherReaderCounts.set(teacherId, (teacherReaderCounts.get(teacherId) ?? 0) + uniqueReaders);
  });
  await batch.commit();

  const teacherScores = new Map<string, number>();
  const teacherBatch = db.batch();
  teacherMaterialScores.forEach((scores, teacherId) => {
    const avg = scores.reduce((a, b) => a + b, 0) / scores.length;
    teacherScores.set(teacherId, avg);
    teacherBatch.update(db.collection(COLLECTIONS.TEACHERS).doc(teacherId), {
      engagementScore: avg,
      totalReaders: teacherReaderCounts.get(teacherId) ?? 0,
    });
  });
  await teacherBatch.commit();

  return teacherScores;
}
