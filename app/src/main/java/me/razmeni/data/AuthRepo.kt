package me.razmeni.data

import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AuthRepo {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUid: String?
        get() = auth.currentUser?.uid

    val currentUsername: String
        get() = auth.currentUser?.email?.substringBefore("@").orEmpty()

    private fun emailFor(username: String) = "${username.trim().lowercase()}@razmeni.me"

    suspend fun login(username: String, password: String) {
        auth.signInWithEmailAndPassword(emailFor(username), password).await()
    }

    suspend fun register(
        username: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        photo: Bitmap
    ) {
        val user = auth.createUserWithEmailAndPassword(emailFor(username), password).await().user!!
        try {
            val profile = mapOf(
                "username" to username.trim().lowercase(),
                "firstName" to firstName.trim(),
                "lastName" to lastName.trim(),
                "phone" to phone.trim(),
                "photoUrl" to ImageUploader.upload(photo),
                "points" to 0L
            )
            db.collection("users").document(user.uid).set(profile).await()
        } catch (e: Exception) {
            // brišemo nalog da korisničko ime ne bi ostalo zauzeto bez profila
            user.delete().await()
            throw e
        }
    }

    fun logout() {
        auth.signOut()
    }
}
