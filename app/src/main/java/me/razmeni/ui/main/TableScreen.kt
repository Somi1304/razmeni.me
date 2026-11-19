package me.razmeni.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.razmeni.data.Exchange
import me.razmeni.data.SortBy
import me.razmeni.data.avgRating
import me.razmeni.data.exchangeType
import me.razmeni.data.ratingCount
import me.razmeni.ui.common.TypeBadge
import me.razmeni.util.distanceMeters
import me.razmeni.util.formatDistance
import me.razmeni.util.formatRate
import me.razmeni.util.formatRating

@Composable
fun TableScreen(vm: MainViewModel, onExchangeClick: (String) -> Unit) {
    val all by vm.exchanges.collectAsState()
    val visible by vm.visibleExchanges.collectAsState()
    val sort by vm.sort.collectAsState()
    val location by vm.location.collectAsState()

    if (all.isEmpty()) {
        EmptyTable(busy = vm.busy, onSeed = vm::seedDemo)
    } else {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Prikazano ${visible.size} od ${all.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = vm::seedDemo, enabled = !vm.busy) {
                        Text("Ubaci demo")
                    }
                    Button(
                        onClick = vm::deleteAllExchanges,
                        enabled = !vm.busy,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Obriši sve")
                    }
                }
            }
            HeaderRow(sort, onSellClick = vm::sortBySell, onDistanceClick = vm::sortByDistance)
            LazyColumn(Modifier.fillMaxSize()) {
                itemsIndexed(visible, key = { _, exchange -> exchange.id }) { index, exchange ->
                    val distance = location?.let {
                        distanceMeters(it.latitude, it.longitude, exchange.lat, exchange.lng)
                    }
                    ExchangeRow(
                        exchange = exchange,
                        distance = distance,
                        striped = index % 2 == 1,
                        onClick = { onExchangeClick(exchange.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(sort: SortBy, onSellClick: () -> Unit, onDistanceClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        HeaderCell("Naziv", 1.8f)
        HeaderCell("Tip", 1.7f)
        HeaderCell("Prodajni" + arrow(sort, SortBy.SELL_ASC, SortBy.SELL_DESC), 1.4f, onSellClick)
        HeaderCell("Ocena", 0.9f)
        HeaderCell("Udalj." + arrow(sort, SortBy.DISTANCE_ASC, SortBy.DISTANCE_DESC), 1.2f, onDistanceClick)
    }
}

@Composable
private fun RowScope.HeaderCell(text: String, weight: Float, onClick: (() -> Unit)? = null) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (onClick != null) FontWeight.Bold else FontWeight.Medium,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier
            .weight(weight)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    )
}

private fun arrow(sort: SortBy, asc: SortBy, desc: SortBy) = when (sort) {
    asc -> " ↑"
    desc -> " ↓"
    else -> ""
}

@Composable
private fun ExchangeRow(exchange: Exchange, distance: Float?, striped: Boolean, onClick: () -> Unit) {
    val background =
        if (striped) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = exchange.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.8f)
        )
        Box(Modifier.weight(1.7f)) {
            TypeBadge(exchange.exchangeType())
        }
        Text(
            text = formatRate(exchange.sellEur),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1.4f)
        )
        Text(
            text = if (exchange.ratingCount() == 0) "-" else formatRating(exchange.avgRating()),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            text = distance?.let { formatDistance(it) } ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
private fun EmptyTable(busy: Boolean, onSeed: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Još nema menjačnica", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Dodaj prvu menjačnicu dugmetom + na mapi ili ubaci tri primera iz centra Niša.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSeed, enabled = !busy) {
            Text("Ubaci demo podatke")
        }
    }
}
