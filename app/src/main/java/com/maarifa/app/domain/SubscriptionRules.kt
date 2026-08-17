package com.maarifa.app.domain

import com.maarifa.app.data.model.PlanType
import com.maarifa.app.data.model.Subscription
import com.maarifa.app.data.model.SubscriptionStatus
import com.maarifa.app.util.DateUtils
import java.util.Date

/**
 * Pure, deterministic subscription rules. No network/Firestore calls here so this can be
 * unit tested directly. The SERVER (Cloud Functions) enforces the authoritative copy of
 * these same rules when it verifies a payment and activates a subscription — the client
 * copy is only used to render correct UI (countdowns, renewal prompts, expiry banners).
 */
object SubscriptionRules {

    fun planFor(type: PlanType) = type

    fun computeEndDate(startDate: Date, plan: PlanType): Date =
        DateUtils.addDays(startDate, plan.durationDays)

    fun isActive(subscription: Subscription?): Boolean {
        if (subscription == null) return false
        return subscription.status == SubscriptionStatus.ACTIVE.name && !DateUtils.isExpired(subscription.endDate)
    }

    /** Renewal is allowed at any time — before OR after expiry (PRD 8.4/13). */
    fun canRenew(subscription: Subscription?): Boolean = true

    /**
     * When renewing before expiry, the new period should stack on top of the remaining
     * days rather than starting fresh from "now" — otherwise a student loses paid-for time.
     * When renewing after expiry (or with no prior subscription) it starts from now.
     */
    fun renewalStartDate(previous: Subscription?): Date {
        val prevEnd = previous?.endDate
        return if (prevEnd != null && prevEnd.after(Date())) prevEnd else Date()
    }

    fun daysRemaining(subscription: Subscription?): Long =
        if (subscription == null) 0 else maxOf(0, DateUtils.daysUntil(subscription.endDate))

    /** True inside the last 3 days of an active plan — used to trigger the expiry-warning notification. */
    fun isNearingExpiry(subscription: Subscription?): Boolean {
        val days = daysRemaining(subscription)
        return isActive(subscription) && days in 0..3
    }
}
