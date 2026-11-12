package me.razmeni.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object UserRepo {
    private val users = FirebaseFirestore.getInstance().collection("users")

    fun listenAll(): Flow<List<User>> = callbackFlow {
        val registration = users.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else if (snapshot != null) {
                try {
                    trySend(snapshot.toObjects(User::class.java))
                } catch (e: Exception) {
                    close(e)
                }
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun updateLocation(lat: Double, lng: Double) {
        users.document(AuthRepo.currentUid!!)
            .update(mapOf("lat" to lat, "lng" to lng, "lastLocationAt" to FieldValue.serverTimestamp()))
            .await()
    }

    fun addPoints(batch: WriteBatch, uid: String, points: Long) {
        batch.update(users.document(uid), "points", FieldValue.increment(points))
    }
}
