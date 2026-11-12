package me.razmeni.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.razmeni.data.DateField
import me.razmeni.data.ExchangeType
import me.razmeni.data.Filters
import me.razmeni.data.User
import me.razmeni.ui.common.FormField
import me.razmeni.ui.common.RatingStars
import me.razmeni.ui.common.SectionTitle
import me.razmeni.util.formatDistance
import me.razmeni.util.formatRate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    current: Filters,
    users: List<User>,
    myUid: String,
    onApply: (Filters) -> Unit,
    onDismiss: () -> Unit
) {
    var filters by remember { mutableStateOf(current) }
    var minSell by remember { mutableStateOf(current.minSell?.let { formatRate(it) }.orEmpty()) }
    var maxSell by remember { mutableStateOf(current.maxSell?.let { formatRate(it) }.orEmpty()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Text("Filteri", style = MaterialTheme.typography.titleLarge)

            SectionTitle("Autor")
            AuthorFilter(filters.authorId, users, myUid) { filters = filters.copy(authorId = it) }

            SectionTitle("Tip")
            TypeFilter(filters.types) { filters = filters.copy(types = it) }

            SectionTitle("Prodajni kurs")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormField(minSell, { minSell = it }, "od", Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                FormField(maxSell, { maxSell = it }, "do", Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
            }

            SectionTitle("Minimalna ocena")
            RatingStars(
                rating = filters.minRating,
                size = 32.dp,
                onRate = { filters = filters.copy(minRating = if (it == filters.minRating) 0 else it) }
            )

            SectionTitle("Datum")
            DateFilter(filters) { filters = it }

            SectionTitle("Radijus")
            RadiusFilter(filters.radiusKm) { filters = filters.copy(radiusKm = it) }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        filters = Filters(query = filters.query)
                        minSell = ""
                        maxSell = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Resetuj")
                }
                Button(
                    onClick = {
                        onApply(
                            filters.copy(
                                minSell = minSell.replace(',', '.').toDoubleOrNull(),
                                maxSell = maxSell.replace(',', '.').toDoubleOrNull()
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Primeni")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthorFilter(authorId: String?, users: List<User>, myUid: String, onChange: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val otherUser = users.find { it.id == authorId && it.id != myUid }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = authorId == null, onClick = { onChange(null) }, label = { Text("Svi") })
        FilterChip(selected = authorId == myUid, onClick = { onChange(myUid) }, label = { Text("Moje") })
        Box {
            FilterChip(
                selected = otherUser != null,
                onClick = { expanded = true },
                label = { Text(otherUser?.username ?: "Korisnik") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                users.sortedBy { it.username }.forEach { user ->
                    DropdownMenuItem(
                        text = { Text(user.username) },
                        onClick = {
                            onChange(user.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeFilter(selected: Set<ExchangeType>, onChange: (Set<ExchangeType>) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ExchangeType.entries.forEach { type ->
            FilterChip(
                selected = type in selected,
                onClick = { onChange(if (type in selected) selected - type else selected + type) },
                label = { Text(type.label) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFilter(filters: Filters, onChange: (Filters) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = filters.dateField == DateField.CREATED,
            onClick = { onChange(filters.copy(dateField = DateField.CREATED)) },
            label = { Text("Kreirano") }
        )
        FilterChip(
            selected = filters.dateField == DateField.LAST_INTERACTION,
            onClick = { onChange(filters.copy(dateField = DateField.LAST_INTERACTION)) },
            label = { Text("Poslednja interakcija") }
        )
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = { showPicker = true }) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Izaberi period")
        }
        if (filters.fromMillis != null) {
            TextButton(onClick = { onChange(filters.copy(fromMillis = null, toMillis = null)) }) {
                Text("Poništi")
            }
        }
    }
    Text(
        text = "Period: " + rangeText(filters.fromMillis, filters.toMillis),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (showPicker) {
        DateRangeDialog(
            from = filters.fromMillis,
            to = filters.toMillis,
            onConfirm = { from, to ->
                onChange(filters.copy(fromMillis = from, toMillis = to))
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeDialog(
    from: Long?,
    to: Long?,
    onConfirm: (Long?, Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = from,
        initialSelectedEndDateMillis = to
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(state.selectedStartDateMillis, state.selectedEndDateMillis) },
                enabled = state.selectedStartDateMillis != null
            ) {
                Text("U redu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Otkaži") }
        }
    ) {
        DateRangePicker(
            state = state,
            title = {
                Text("Izaberi period", modifier = Modifier.padding(start = 24.dp, top = 16.dp))
            },
            headline = {
                Text(
                    text = rangeText(state.selectedStartDateMillis, state.selectedEndDateMillis)
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
                )
            },
            showModeToggle = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RadiusFilter(radiusKm: Float?, onChange: (Float?) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Samo u blizini", modifier = Modifier.weight(1f))
        Switch(checked = radiusKm != null, onCheckedChange = { onChange(if (it) 1f else null) })
    }
    if (radiusKm != null) {
        Text(
            text = "Do ${formatDistance(radiusKm * 1000)} od tebe",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = radiusKm,
            onValueChange = { onChange(it) },
            valueRange = 0.5f..10f,
            steps = 18
        )
    }
}

private fun rangeText(from: Long?, to: Long?): String = when {
    from == null -> "bilo kada"
    to == null -> "od ${formatDay(from)}"
    else -> "${formatDay(from)} - ${formatDay(to)}"
}

private fun formatDay(utcMillis: Long): String =
    SimpleDateFormat("dd.MM.yyyy.", Locale.getDefault())
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(utcMillis))
