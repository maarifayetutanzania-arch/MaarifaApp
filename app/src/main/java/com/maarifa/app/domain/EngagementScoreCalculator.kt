package com.maarifa.app.domain

import com.maarifa.app.data.model.Engagement
import com.maarifa.app.util.AppConfig
import kotlin.math.ln
import kotlin.math.min

/**
 * Converts raw engagement records into a bounded 0..100 score per material/teacher.
 *
 * Design goals (PRD 8.7 / 14):
 *  - Reward UNIQUE readers more than repeat opens from the same person (prevents a
 *    single user farming "engagement" by reopening a file).
 *  - Give diminishing returns on read count (log curve) so one obsessive reader can't
 *    outweigh fifty distinct students who each read once.
 *  - Weight in actual reading progress/time, not just "opened" — a 2-second open that
 *    never scrolls counts far less than a completed read.
 *  - This same function is re-run server-side (functions) as the authoritative source
 *    of truth used for real money in EarningsCalculator; the client copy is for
 *    showing teachers their live score, not for computing payouts itself.
 */
object EngagementScoreCalculator {

    fun scoreForMaterial(records: List<Engagement>): Double {
        if (records.isEmpty()) return 0.0

        val uniqueReaders = records.map { it.userId }.distinct().size
        val totalReads = records.sumOf { it.readCount }
        val avgProgress = records.map { it.progressPercent.coerceIn(0, 100) }.average()

        // De-duplicate rapid-fire re-reads: cap each user's contribution.
        val perUserCappedReads = records.groupBy { it.userId }
            .values
            .sumOf { userRecords -> min(userRecords.sumOf { it.readCount }, MAX_COUNTED_READS_PER_USER) }

        val readerBreadth = uniqueReaders.toDouble() * UNIQUE_READER_WEIGHT
        val depthBonus = ln(1.0 + perUserCappedReads) * DEPTH_WEIGHT
        val completionBonus = (avgProgress / 100.0) * COMPLETION_WEIGHT

        val raw = readerBreadth + depthBonus + completionBonus
        return raw.coerceIn(0.0, 100.0)
    }

    /** Aggregate a teacher's overall score across every material they own. */
    fun scoreForTeacher(perMaterialScores: List<Double>): Double {
        if (perMaterialScores.isEmpty()) return 0.0
        return perMaterialScores.average().coerceIn(0.0, 100.0)
    }

    /** Each teacher's slice of the shared pool, proportional to score among all teachers with content. */
    fun poolShares(teacherScores: Map<String, Double>): Map<String, Double> {
        val total = teacherScores.values.sum()
        if (total <= 0.0) return teacherScores.mapValues { 0.0 }
        return teacherScores.mapValues { (_, score) -> score / total }
    }

    private const val UNIQUE_READER_WEIGHT = 6.0
    private const val DEPTH_WEIGHT = 8.0
    private const val COMPLETION_WEIGHT = 20.0
    private const val MAX_COUNTED_READS_PER_USER = 5
}
