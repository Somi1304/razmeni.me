package me.razmeni.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import me.razmeni.POINTS_ADD
import me.razmeni.POINTS_COMMENT

object ExchangeRepo {
    private val db = FirebaseFirestore.getInstance()
    private val exchanges = db.collection("exchanges")

    fun listen(): Flow<List<Exchange>> = callbackFlow {
        val registration = exchanges.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else if (snapshot != null) {
                try {
                    trySend(snapshot.toObjects(Exchange::class.java))
                } catch (e: Exception) {
                    close(e)
                }
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun add(exchange: Exchange, points: Long = POINTS_ADD) {
        val uid = AuthRepo.currentUid!!
        val batch = db.batch()
        batch.set(
            exchanges.document(),
            mapOf(
                "name" to exchange.name,
                "type" to exchange.type,
                "address" to exchange.address,
                "description" to exchange.description,
                "workingHours" to exchange.workingHours,
                "buyEur" to exchange.buyEur,
                "sellEur" to exchange.sellEur,
                "lat" to exchange.lat,
                "lng" to exchange.lng,
                "photoUrl" to exchange.photoUrl,
                "authorId" to uid,
                "authorUsername" to AuthRepo.currentUsername,
                "createdAt" to FieldValue.serverTimestamp(),
                "lastInteractionAt" to FieldValue.serverTimestamp(),
                "ratings" to exchange.ratings,
                "comments" to exchange.comments.map {
                    mapOf(
                        "authorUsername" to it.authorUsername,
                        "text" to it.text,
                        "createdAt" to (it.createdAt ?: Timestamp.now())
                    )
                },
                "visitedBy" to emptyList<String>()
            )
        )
        if (points > 0) UserRepo.addPoints(batch, uid, points)
        batch.commit().await()
    }

    suspend fun interact(exchangeId: String, changes: Map<String, Any>, points: Long) {
        val uid = AuthRepo.currentUid!!
        val batch = db.batch()
        batch.update(
            exchanges.document(exchangeId),
            changes + ("lastInteractionAt" to FieldValue.serverTimestamp())
        )
        if (points > 0) UserRepo.addPoints(batch, uid, points)
        batch.commit().await()
    }

    suspend fun addComment(exchangeId: String, text: String) {
        // serverTimestamp() ne sme unutar arrayUnion, zato Timestamp.now()
        val comment = mapOf(
            "authorUsername" to AuthRepo.currentUsername,
            "text" to text,
            "createdAt" to Timestamp.now()
        )
        interact(exchangeId, mapOf("comments" to FieldValue.arrayUnion(comment)), POINTS_COMMENT)
    }

    suspend fun deleteAll() {
        val snapshot = exchanges.get().await()
        snapshot.documents.chunked(500).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { doc -> batch.delete(doc.reference) }
            batch.commit().await()
        }
    }
}
