package me.razmeni.ui.exchange

import android.graphics.Bitmap
import android.location.Location
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import me.razmeni.data.Exchange
import me.razmeni.data.ExchangeType
import me.razmeni.data.ImageUploader
import me.razmeni.ui.common.FormField
import me.razmeni.ui.common.SectionTitle
import me.razmeni.ui.main.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExchangeScreen(vm: MainViewModel, onBack: () -> Unit) {
    val location by vm.location.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova menjačnica") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { padding ->
        val current = location
        if (current == null) {
            Text(
                text = "Lokacija još nije poznata. Sačekaj da se na mapi pojavi plava tačka.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            )
        } else {
            AddExchangeForm(current, vm, onBack, Modifier.padding(padding))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExchangeForm(
    location: Location,
    vm: MainViewModel,
    onSaved: () -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(ExchangeType.MENJACNICA) }
    var address by rememberSaveable { mutableStateOf("") }
    var buy by rememberSaveable { mutableStateOf("") }
    var sell by rememberSaveable { mutableStateOf("") }
    var hours by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var photo by remember { mutableStateOf<Bitmap?>(null) }

    val buyValue = buy.replace(',', '.').toDoubleOrNull()
    val sellValue = sell.replace(',', '.').toDoubleOrNull()
    val valid = name.isNotBlank() && buyValue != null && buyValue > 0 && sellValue != null && sellValue > 0

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) ImageUploader.decode(context, uri)?.let { photo = it }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        LocationCard(location)
        Spacer(Modifier.height(8.dp))
        FormField(name, { name = it }, "Naziv")

        SectionTitle("Tip")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExchangeType.entries.forEach { option ->
                FilterChip(
                    selected = type == option,
                    onClick = { type = option },
                    label = { Text(option.label) }
                )
            }
        }

        FormField(address, { address = it }, "Adresa")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FormField(buy, { buy = it }, "Kupovni kurs", Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
            FormField(sell, { sell = it }, "Prodajni kurs", Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
        }
        FormField(hours, { hours = it }, "Radno vreme (npr. 08-20, nedeljom zatvoreno)")
        FormField(description, { description = it }, "Opis", singleLine = false)

        SectionTitle("Slika (opciono)")
        PhotoRow(
            photo = photo,
            onPick = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val exchange = Exchange(
                    name = name.trim(),
                    type = type.name,
                    address = address.trim(),
                    description = description.trim(),
                    workingHours = hours.trim(),
                    buyEur = buyValue ?: 0.0,
                    sellEur = sellValue ?: 0.0,
                    lat = location.latitude,
                    lng = location.longitude
                )
                vm.addExchange(exchange, photo, onSaved)
            },
            enabled = valid && !vm.busy,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (vm.busy) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Sačuvaj")
            }
        }
    }
}

@Composable
private fun LocationCard(location: Location) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Lokacija: %.5f, %.5f".format(Locale.US, location.latitude, location.longitude))
        }
    }
}

@Composable
private fun PhotoRow(photo: Bitmap?, onPick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (photo != null) {
            AsyncImage(
                model = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.width(16.dp))
        }
        OutlinedButton(onClick = onPick) {
            Text(if (photo == null) "Izaberi iz galerije" else "Promeni sliku")
        }
    }
}
