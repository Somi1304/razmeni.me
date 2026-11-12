package me.razmeni.ui.auth

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.launch
import me.razmeni.data.AuthRepo

class AuthViewModel : ViewModel() {
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var photo by mutableStateOf<Bitmap?>(null)

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        submit(onSuccess) { AuthRepo.login(username, password) }
    }

    fun register(
        username: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        onSuccess: () -> Unit
    ) {
        val image = photo ?: return
        submit(onSuccess) { AuthRepo.register(username, password, firstName, lastName, phone, image) }
    }

    private fun submit(onSuccess: () -> Unit, block: suspend () -> Unit) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                block()
                onSuccess()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "auth", e)
                error = when (e) {
                    is FirebaseAuthUserCollisionException -> "Korisničko ime je zauzeto"
                    is FirebaseAuthInvalidCredentialsException,
                    is FirebaseAuthInvalidUserException -> "Pogrešno korisničko ime ili šifra"
                    else -> "Došlo je do greške, pokušaj ponovo"
                }
            }
            loading = false
        }
    }
}
