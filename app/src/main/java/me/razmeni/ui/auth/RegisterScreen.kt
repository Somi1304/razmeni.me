package me.razmeni.ui.auth

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import me.razmeni.data.ImageUploader
import me.razmeni.ui.common.FormField
import me.razmeni.ui.common.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    val valid = username.isNotBlank() && password.length >= 6 && firstName.isNotBlank() &&
        lastName.isNotBlank() && phone.isNotBlank() && vm.photo != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registracija") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FormField(username, { username = it }, "Korisničko ime")
            FormField(
                value = password,
                onValueChange = { password = it },
                label = "Šifra (najmanje 6 znakova)",
                keyboardType = KeyboardType.Password,
                isPassword = true
            )
            FormField(firstName, { firstName = it }, "Ime")
            FormField(lastName, { lastName = it }, "Prezime")
            FormField(phone, { phone = it }, "Telefon", keyboardType = KeyboardType.Phone)
            Spacer(Modifier.height(16.dp))

            PhotoPicker(photo = vm.photo, name = "$firstName $lastName", onPhoto = { vm.photo = it })

            vm.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { vm.register(username, password, firstName, lastName, phone, onRegistered) },
                enabled = valid && !vm.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (vm.loading) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Registruj se")
                }
            }
        }
    }
}

@Composable
private fun PhotoPicker(photo: Bitmap?, name: String, onPhoto: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) ImageUploader.decode(context, uri)?.let(onPhoto)
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) onPhoto(bitmap)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        UserAvatar(image = photo, name = name, size = 112.dp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                Text("Galerija")
            }
            OutlinedButton(onClick = { camera.launch(null) }) {
                Text("Kamera")
            }
        }
        if (photo == null) {
            Text(
                text = "Izaberi sliku profila",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
