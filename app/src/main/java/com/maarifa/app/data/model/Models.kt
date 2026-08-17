package com.maarifa.app.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * All model classes use `var` + default values so Firestore's no-arg-constructor
 * reflection deserializer (toObject<T>()) can populate them directly.
 */

data class User(
    @DocumentId var userId: String = "",
    var fullName: String = "",
    var phoneNumber: String = "",
    var email: String = "",
    var authProvider: String = AuthProvider.EMAIL.name,
    var role: String = UserRole.STUDENT.name,
    var region: String = "",
    var schoolName: String? = null,
    var formClass: String = FormClass.FORM_1.name,
    var status: String = AccountStatus.ACTIVE.name,
    var fcmToken: String? = null,
    @ServerTimestamp var createdAt: Date? = null
) {
    val roleEnum: UserRole get() = UserRole.from(role)
}

data class Teacher(
    @DocumentId var teacherId: String = "", // same value as userId
    var userId: String = "",
    var verificationStatus: String = TeacherVerificationStatus.PENDING.name,
    var verificationNotes: String = "",
    var totalUploads: Int = 0,
    var totalReaders: Int = 0,
    var engagementScore: Double = 0.0,
    var earningsBalanceTzs: Long = 0,
    var payoutStatus: String = PayoutStatus.GENERATED.name,
    var bio: String = "",
    var subjectsTaught: List<String> = emptyList(),
    @ServerTimestamp var createdAt: Date? = null
)

data class Material(
    @DocumentId var materialId: String = "",
    var teacherId: String = "",
    var teacherName: String = "",
    var title: String = "",
    var description: String = "",
    var form: String = FormClass.FORM_1.name,
    var subject: String = "",
    var topic: String = "",
    var fileUrl: String = "",
    var fileType: String = MaterialFileType.PDF.name,
    var fileSizeBytes: Long = 0,
    var pageCount: Int = 0,
    var status: String = MaterialStatus.PENDING_REVIEW.name,
    var rejectionReason: String = "",
    var uniqueReaderCount: Int = 0,
    var totalReadCount: Int = 0,
    var saveCount: Int = 0,
    @ServerTimestamp var createdAt: Date? = null,
    var approvedAt: Date? = null
)

data class Subscription(
    @DocumentId var subscriptionId: String = "",
    var userId: String = "",
    var planType: String = PlanType.MONTHLY.name,
    var amountTzs: Long = 0,
    var durationDays: Int = 0,
    var provider: String = "",
    var channel: String = PaymentChannel.MOBILE_MONEY.name,
    var transactionId: String = "",
    var startDate: Date? = null,
    var endDate: Date? = null,
    var status: String = SubscriptionStatus.PENDING_PAYMENT.name,
    var verifiedAt: Date? = null,
    @ServerTimestamp var createdAt: Date? = null
) {
    val isCurrentlyActive: Boolean
        get() = status == SubscriptionStatus.ACTIVE.name &&
            endDate != null && endDate!!.after(Date())
}

data class Download(
    @DocumentId var downloadId: String = "",
    var userId: String = "",
    var materialId: String = "",
    var localStatus: String = DownloadLocalStatus.QUEUED.name,
    var localFileName: String = "",
    var bytesTotal: Long = 0,
    var bytesDownloaded: Long = 0,
    @ServerTimestamp var downloadedAt: Date? = null,
    var lastOpenedAt: Date? = null
)

data class Engagement(
    @DocumentId var engagementId: String = "",
    var userId: String = "",
    var materialId: String = "",
    var teacherId: String = "",
    var readCount: Int = 0,
    var readingTimeSeconds: Long = 0,
    var progressPercent: Int = 0,
    var lastAccessedAt: Date? = null
)

data class Payout(
    @DocumentId var payoutId: String = "",
    var teacherId: String = "",
    var teacherName: String = "",
    var period: String = "", // e.g. "2026-08"
    var engagementSharePercent: Double = 0.0,
    var calculatedAmountTzs: Long = 0,
    var status: String = PayoutStatus.GENERATED.name,
    var approvedBy: String = "",
    var transactionId: String = "",
    @ServerTimestamp var createdAt: Date? = null
)

data class NotificationItem(
    @DocumentId var notificationId: String = "",
    var userId: String = "",
    var category: String = NotificationCategory.NEW_CONTENT.name,
    var title: String = "",
    var body: String = "",
    var read: Boolean = false,
    var relatedId: String = "",
    @ServerTimestamp var createdAt: Date? = null
)
