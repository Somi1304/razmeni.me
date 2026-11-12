package me.razmeni.ui.exchange

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import me.razmeni.VISIT_M
import me.razmeni.data.AuthRepo
import me.razmeni.data.Comment
import me.razmeni.data.Exchange
import me.razmeni.data.avgRating
import me.razmeni.data.exchangeType
import me.razmeni.data.ratingCount
import me.razmeni.ui.common.FormField
import me.razmeni.ui.common.RatingStars
import me.razmeni.ui.common.SectionTitle
import me.razmeni.ui.common.TypeBadge
import me.razmeni.ui.main.MainViewModel
import me.razmeni.util.distanceMeters
import me.razmeni.util.formatDate
import me.razmeni.util.formatDistance
import me.razmeni.util.formatRate
import me.razmeni.util.formatRating
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeDetailsScreen(id: String, vm: MainViewModel, onBack: () -> Unit) {
    val exchanges by vm.exchanges.collectAsState()
    val location by vm.location.collectAsState()
    val exchange = exchanges.find { it.id == id }

    LaunchedEffect(Unit) { vm.start() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exchange?.name.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { padding ->
        if (exchange == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            DetailsContent(exchange, location, vm, Modifier.padding(padding))
        }
    }
}

@Composable
private fun DetailsContent(
    exchange: Exchange,
    location: Location?,
    vm: MainViewModel,
    modifier: Modifier
) {
    var showRateDialog by remember { mutableStateOf(false) }
    val myUid = AuthRepo.currentUid.orEmpty()
    val distance = location?.let { distanceMeters(it.latitude, it.longitude, exchange.lat, exchange.lng) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Header(exchange, distance) }
        item { RateCard(exchange, enabled = !vm.busy, onUpdate = { showRateDialog = true }) }
        item {
            RatingCard(
                exchange = exchange,
                myRating = exchange.ratings[myUid]?.toInt() ?: 0,
                enabled = !vm.busy,
                onRate = { vm.rate(exchange, it) }
            )
        }
        item {
            VisitButton(
                distance = distance,
                visited = myUid in exchange.visitedBy,
                enabled = !vm.busy,
                onVisit = { vm.visit(exchange) }
            )
        }
        item { CommentInput(enabled = !vm.busy, onSend = { vm.comment(exchange, it) }) }
        if (exchange.comments.isEmpty()) {
            item {
                Text("Još nema komentara.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        items(exchange.comments.reversed()) { CommentCard(it) }
    }

    if (showRateDialog) {
        UpdateRateDialog(
            exchange = exchange,
            onConfirm = { buy, sell ->
                vm.updateRate(exchange, buy, sell)
                showRateDialog = false
            },
            onDismiss = { showRateDialog = false }
        )
    }
}

@Composable
private fun Header(exchange: Exchange, distance: Float?) {
    Column {
        if (exchange.photoUrl.isNotBlank()) {
            AsyncImage(
                model = exchange.photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.height(12.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(exchange.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TypeBadge(exchange.exchangeType())
        }
        InfoLine(Icons.Default.LocationOn, exchange.address.ifBlank { "Adresa nije uneta" })
        InfoLine(Icons.Default.DateRange, exchange.workingHours.ifBlank { "Radno vreme nije uneto" })
        InfoLine(Icons.Default.Place, "Udaljenost: " + (distance?.let { formatDistance(it) } ?: "nepoznata"))
        InfoLine(Icons.Default.Person, "Autor: ${exchange.authorUsername}, ${formatDate(exchange.createdAt)}")
        if (exchange.description.isNotBlank()) {
            Text(exchange.description, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun InfoLine(icon: ImageVector, text: String) {
    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RateCard(exchange: Exchange, enabled: Boolean, onUpdate: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Kurs evra", style = MaterialTheme.typography.titleMedium)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                RateValue("Kupovni", exchange.buyEur, Modifier.weight(1f))
                RateValue("Prodajni", exchange.sellEur, Modifier.weight(1f))
            }
            Text("Ažurirano: ${formatDate(exchange.lastInteractionAt)}", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onUpdate, enabled = enabled) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ažuriraj kurs")
            }
        }
    }
}

@Composable
private fun RateValue(label: String, value: Double, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(
            text = "${formatRate(value)} din",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RatingCard(exchange: Exchange, myRating: Int, enabled: Boolean, onRate: (Int) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RatingStars(exchange.avgRating().roundToInt(), size = 20.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (exchange.ratingCount() == 0) "Još nema ocena"
                    else "${formatRating(exchange.avgRating())} od 5, broj ocena: ${exchange.ratingCount()}"
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (myRating == 0) "Oceni menjačnicu" else "Tvoja ocena",
                style = MaterialTheme.typography.labelLarge
            )
            RatingStars(myRating, size = 36.dp, onRate = if (enabled) onRate else null)
        }
    }
}

@Composable
private fun VisitButton(distance: Float?, visited: Boolean, enabled: Boolean, onVisit: () -> Unit) {
    val label = when {
        visited -> "Već posećeno"
        distance == null -> "Čekam lokaciju..."
        distance > VISIT_M -> "Priđi bliže (još ${formatDistance(distance - VISIT_M)})"
        else -> "Ovde sam"
    }
    val near = distance != null && distance <= VISIT_M
    Button(
        onClick = onVisit,
        enabled = enabled && near && !visited,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun CommentInput(enabled: Boolean, onSend: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    Column {
        SectionTitle("Komentari")
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Napiši komentar") },
                maxLines = 3,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    onSend(text)
                    text = ""
                },
                enabled = enabled && text.isNotBlank()
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Pošalji")
            }
        }
    }
}

@Composable
private fun CommentCard(comment: Comment) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    comment.authorUsername,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    formatDate(comment.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(comment.text, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun UpdateRateDialog(exchange: Exchange, onConfirm: (Double, Double) -> Unit, onDismiss: () -> Unit) {
    var buy by remember { mutableStateOf(formatRate(exchange.buyEur)) }
    var sell by remember { mutableStateOf(formatRate(exchange.sellEur)) }
    val buyValue = buy.replace(',', '.').toDoubleOrNull()
    val sellValue = sell.replace(',', '.').toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novi kurs evra") },
        text = {
            Column {
                FormField(buy, { buy = it }, "Kupovni", keyboardType = KeyboardType.Decimal)
                FormField(sell, { sell = it }, "Prodajni", keyboardType = KeyboardType.Decimal)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (buyValue != null && sellValue != null) onConfirm(buyValue, sellValue) },
                enabled = buyValue != null && sellValue != null
            ) {
                Text("Sačuvaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Otkaži") }
        }
    )
}
