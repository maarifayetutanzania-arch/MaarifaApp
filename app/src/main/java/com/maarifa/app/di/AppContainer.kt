package com.maarifa.app.di

import android.content.Context
import com.maarifa.app.data.remote.FirebaseAuthService
import com.maarifa.app.data.repository.AuthRepository
import com.maarifa.app.data.repository.DownloadRepository
import com.maarifa.app.data.repository.EngagementRepository
import com.maarifa.app.data.repository.MaterialRepository
import com.maarifa.app.data.repository.NotificationRepository
import com.maarifa.app.data.repository.PaymentRepository
import com.maarifa.app.data.repository.PayoutRepository
import com.maarifa.app.data.repository.SubscriptionRepository
import com.maarifa.app.data.repository.TeacherRepository
import com.maarifa.app.util.FileDownloadManager

/**
 * Deliberately hand-rolled singleton container instead of Hilt/Dagger: it keeps the
 * project buildable with zero annotation-processor configuration, which matters most
 * for a project you'll be opening cold in Android Studio for the first time.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val authService = FirebaseAuthService()
    val fileDownloadManager = FileDownloadManager(appContext)

    val authRepository = AuthRepository(authService)
    val materialRepository = MaterialRepository()
    val subscriptionRepository = SubscriptionRepository()
    val paymentRepository = PaymentRepository()
    val downloadRepository = DownloadRepository(fileDownloadManager)
    val engagementRepository = EngagementRepository()
    val teacherRepository = TeacherRepository()
    val payoutRepository = PayoutRepository()
    val notificationRepository = NotificationRepository()

    companion object {
        @Volatile private var instance: AppContainer? = null

        fun get(context: Context): AppContainer =
            instance ?: synchronized(this) {
                instance ?: AppContainer(context).also { instance = it }
            }
    }
}
