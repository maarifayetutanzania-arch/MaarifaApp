package com.maarifa.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.maarifa.app.data.model.NotificationItem
import com.maarifa.app.util.FirestorePaths
import com.maarifa.app.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val notifications get() = firestore.collection(FirestorePaths.NOTIFICATIONS)
    private val users get() = firestore.collection(FirestorePaths.USERS)

    fun observeNotifications(userId: String): Flow<Resource<List<NotificationItem>>> = callbackFlow {
        val registration = notifications.whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load notifications"))
                    return@addSnapshotListener
                }
                trySend(Resource.Success(snapshot?.toObjects(NotificationItem::class.java).orEmpty()))
            }
        awaitClose { registration.remove() }
    }

    suspend fun markRead(notificationId: String) {
        try { notifications.document(notificationId).update("read", true).await() } catch (_: Exception) {}
    }

    /** Called from MainActivity/App start so Cloud Functions can push FCM notifications
     * (new content, approvals, expiry warnings, payment/payout issues — PRD 8.9) to this device. */
    suspend fun saveFcmToken(userId: String, token: String) {
        try { users.document(userId).update("fcmToken", token).await() } catch (_: Exception) {}
    }
}
