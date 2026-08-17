package com.maarifa.app.domain

import com.maarifa.app.data.model.Material
import com.maarifa.app.data.model.MaterialStatus
import com.maarifa.app.data.model.Subscription

/**
 * Single choke point every screen goes through before showing/downloading content.
 * Centralizing this avoids the classic bug of one screen forgetting to re-check
 * subscription state. Mirrors PRD 8.2/8.3/12: only approved content is visible,
 * offline access requires content that was already authorized while online.
 */
object AccessControlUseCase {

    sealed class AccessDecision {
        data object Allowed : AccessDecision()
        data object MaterialNotApproved : AccessDecision()
        data object SubscriptionRequired : AccessDecision()
        data object SubscriptionExpired : AccessDecision()
    }

    fun canViewOnline(material: Material, subscription: Subscription?): AccessDecision {
        if (material.status != MaterialStatus.APPROVED.name) return AccessDecision.MaterialNotApproved
        if (SubscriptionRules.isActive(subscription)) return AccessDecision.Allowed
        return if (subscription == null) AccessDecision.SubscriptionRequired else AccessDecision.SubscriptionExpired
    }

    /** Same gate as online viewing — a download can only be *initiated* for content
     * the user is currently authorized to view. Once downloaded, ReaderScreen reads the
     * local file directly so a lapsed subscription doesn't retroactively delete files,
     * but it DOES still block opening (see canOpenDownloadedFile). */
    fun canStartDownload(material: Material, subscription: Subscription?): AccessDecision =
        canViewOnline(material, subscription)

    /**
     * PRD 8.3: "offline access only for content already authorized while online" — this is
     * intentionally lenient (doesn't force a network check every open) but does require the
     * subscription to not be in a definitively cancelled/failed state at last sync.
     */
    fun canOpenDownloadedFile(subscription: Subscription?): Boolean {
        if (subscription == null) return false
        return subscription.status != com.maarifa.app.data.model.SubscriptionStatus.CANCELLED.name
    }
}
