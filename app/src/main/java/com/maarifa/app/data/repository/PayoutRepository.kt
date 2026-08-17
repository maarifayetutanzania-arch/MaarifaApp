package com.maarifa.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.maarifa.app.data.model.Payout
import com.maarifa.app.util.FirestorePaths
import com.maarifa.app.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PayoutRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection get() = firestore.collection(FirestorePaths.PAYOUTS)

    /** All payout records ever generated for this teacher (by the scheduled Cloud
     * Function), newest first — teachers see this as a read-only earnings history. */
    fun observePayouts(teacherId: String): Flow<Resource<List<Payout>>> = callbackFlow {
        val registration = collection.whereEqualTo("teacherId", teacherId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Failed to load payouts"))
                    return@addSnapshotListener
                }
                trySend(Resource.Success(snapshot?.toObjects(Payout::class.java).orEmpty()))
            }
        awaitClose { registration.remove() }
    }
}
