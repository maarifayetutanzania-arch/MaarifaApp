package com.maarifa.app.util

object FirestorePaths {
    const val USERS = "users"
    const val TEACHERS = "teachers"
    const val MATERIALS = "materials"
    const val SUBSCRIPTIONS = "subscriptions"
    const val DOWNLOADS = "downloads"
    const val ENGAGEMENT = "engagement"
    const val PAYOUTS = "payouts"
    const val NOTIFICATIONS = "notifications"
}

object StorageePaths {
    const val MATERIALS_ROOT = "materials"
}

object CloudFunctions {
    // Matches functions/src/index.ts exports — see /functions in project root.
    const val INITIATE_PAYMENT = "initiatePayment"
    const val VERIFY_PAYMENT_STATUS = "getPaymentStatus"
}

object AppConfig {
    /** Revenue split enforced identically server-side (functions/src/earnings.ts) — this
     * client-side copy is for display purposes ONLY and never used to compute real money. */
    const val TEACHER_POOL_SHARE = 0.75
    const val PLATFORM_SHARE = 0.25

    const val MIN_ENGAGEMENT_READ_GAP_MINUTES = 2 // reads from the same user closer than this are treated as one read (anti duplicate-engagement)
}
