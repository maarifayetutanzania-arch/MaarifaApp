package com.maarifa.app.domain

import com.maarifa.app.util.AppConfig
import kotlin.math.roundToLong

data class TeacherEarningsResult(
    val teacherId: String,
    val sharePercent: Double,
    val amountTzs: Long
)

data class EarningsRunResult(
    val periodRevenueTzs: Long,
    val teacherPoolTzs: Long,
    val platformShareTzs: Long,
    val perTeacher: List<TeacherEarningsResult>
)

/**
 * Implements PRD 8.8 / 14 exactly:
 *   - 75% of verified subscription revenue -> teacher pool
 *   - 25% -> platform
 *   - Teacher pool is split by each teacher's share of total engagement score
 *   - Admin reviews/approves the OUTPUT of this function; it never hand-computes the split
 *
 * Pure function of (verified revenue, engagement scores) so it runs identically here (for
 * teacher-facing "estimated earnings" previews) and inside the Cloud Function
 * `generateEarningsAndPayouts` (functions/src/earnings.ts), which is the only place a real
 * Payout document is ever written.
 */
object EarningsCalculator {

    fun run(verifiedRevenueTzs: Long, teacherEngagementScores: Map<String, Double>): EarningsRunResult {
        val teacherPool = (verifiedRevenueTzs * AppConfig.TEACHER_POOL_SHARE).roundToLong()
        val platformShare = verifiedRevenueTzs - teacherPool

        val shares = EngagementScoreCalculator.poolShares(teacherEngagementScores)
        val perTeacher = shares.map { (teacherId, share) ->
            TeacherEarningsResult(
                teacherId = teacherId,
                sharePercent = share * 100.0,
                amountTzs = (teacherPool * share).roundToLong()
            )
        }.sortedByDescending { it.amountTzs }

        return EarningsRunResult(
            periodRevenueTzs = verifiedRevenueTzs,
            teacherPoolTzs = teacherPool,
            platformShareTzs = platformShare,
            perTeacher = perTeacher
        )
    }
}
