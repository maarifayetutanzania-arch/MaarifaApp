package com.maarifa.app.data.model

/** Top-level role stored on the User document. Drives which UI graph the app routes into. */
enum class UserRole { STUDENT, TEACHER, ADMIN, UNKNOWN;
    companion object {
        fun from(value: String?): UserRole = entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}

enum class AuthProvider { GOOGLE, PHONE, EMAIL }

enum class AccountStatus { ACTIVE, SUSPENDED, PENDING }

/** Form I - Form VI as used in Tanzanian secondary education. */
enum class FormClass(val label: String) {
    FORM_1("Form I"), FORM_2("Form II"), FORM_3("Form III"),
    FORM_4("Form IV"), FORM_5("Form V"), FORM_6("Form VI");
}

enum class TeacherVerificationStatus { PENDING, VERIFIED, REJECTED }

enum class MaterialStatus { PENDING_REVIEW, APPROVED, REJECTED }

enum class MaterialFileType { PDF, IMAGE_SCAN }

enum class PlanType(
    val label: String,
    val amountTzs: Long,
    val durationDays: Int
) {
    WEEKLY("Weekly", 3_000, 7),
    MONTHLY("Monthly", 10_000, 30),
    QUARTERLY("Quarterly", 25_000, 90)
}

enum class PaymentChannel { BANK, MOBILE_MONEY }

enum class SubscriptionStatus { PENDING_PAYMENT, ACTIVE, EXPIRED, FAILED, CANCELLED }

enum class PayoutStatus { GENERATED, UNDER_REVIEW, APPROVED, PAID, EXCEPTION }

enum class DownloadLocalStatus { QUEUED, DOWNLOADING, COMPLETE, FAILED }

enum class NotificationCategory { NEW_CONTENT, SUBSCRIPTION_EXPIRY, APPROVAL, REJECTION, EARNINGS, PAYMENT_ISSUE, REPORTED_CONTENT, PAYOUT_EXCEPTION }
