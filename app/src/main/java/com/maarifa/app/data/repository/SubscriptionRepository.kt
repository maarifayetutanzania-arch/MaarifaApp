package com.maarifa.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.maarifa.app.data.model.Subscription
import com.maarifa.app.util.FirestorePaths
import com.maarifa.app.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * PRD 8.5 / 12: "shall not activate a subscription based on user claim alone" — this
 * repository NEVER writes status=ACTIVE from the client. It only (a) reads the current
 * subscription in real time so the UI reflects whatever the backend has verified, and
 * (b) asks PaymentRepository to kick off a payment, which is where the write to
 * PENDING_PAYMENT -> ACTIVE/FAILED happens, entirely inside Cloud Functions.
 */
class SubscriptionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection get() = firestore.collection(FirestorePaths.SUBSCRIPTIONS)

    /** Latest subscription document for this user, updated live as the backend verifies payment. */
    fun observeLatestSubscription(userId: String): Flow<Resource<Subscription?>> = callbackFlow {
        val registration = collection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load subscription"))
                    return@addSnapshotListener
                }
                val sub = snapshot?.documents?.firstOrNull()?.toObject(Subscription::class.java)
                trySend(Resource.Success(sub))
            }
        awaitClose { registration.remove() }
    }

    suspend fun history(userId: String): Resource<List<Subscription>> = try {
        val snap = collection.whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
        Resource.Success(snap.toObjects(Subscription::class.java))
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Failed to load subscription history", e)
    }
}
