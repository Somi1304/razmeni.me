package me.razmeni.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import me.razmeni.data.AuthRepo
import me.razmeni.data.Filters

private val PERMISSIONS = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACCESS_COARSE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
}.toTypedArray()

@Composable
fun MainScreen(
    vm: MainViewModel,
    onAddClick: () -> Unit,
    onExchangeClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val myUid = AuthRepo.currentUid.orEmpty()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var asked by rememberSaveable { mutableStateOf(false) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var locationGranted by remember { mutableStateOf(hasLocationPermission(context)) }

    val location by vm.location.collectAsState()
    val visible by vm.visibleExchanges.collectAsState()
    val filters by vm.filters.collectAsState()
    val users by vm.users.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        locationGranted = hasLocationPermission(context)
    }

    LaunchedEffect(Unit) {
        vm.start()
        if (!asked) {
            asked = true
            permissionLauncher.launch(PERMISSIONS)
        }
    }
    LaunchedEffect(locationGranted) {
        if (locationGranted) vm.startTracking()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { BottomBar(selected = tab, onSelect = { tab = it }) },
        floatingActionButton = {
            if (tab == 0 && locationGranted) {
                FloatingActionButton(onClick = {
                    if (location == null) {
                        Toast.makeText(context, "Čekam lokaciju...", Toast.LENGTH_SHORT).show()
                    } else {
                        onAddClick()
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj menjačnicu")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (tab != 2) {
                SearchRow(
                    query = filters.query,
                    onQueryChange = vm::setQuery,
                    activeFilters = filters.activeCount(),
                    onFiltersClick = { showFilters = true }
                )
            }
            when (tab) {
                0 -> if (locationGranted) {
                    MapScreen(visible, location, onExchangeClick)
                } else {
                    LocationDenied(onAllow = { permissionLauncher.launch(PERMISSIONS) })
                }
                1 -> TableScreen(vm, onExchangeClick)
                2 -> LeaderboardScreen(users, myUid, onLogout)
            }
        }
    }

    if (showFilters) {
        FilterSheet(
            current = filters,
            users = users,
            myUid = myUid,
            onApply = {
                vm.applyFilters(it)
                showFilters = false
            },
            onDismiss = { showFilters = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    activeFilters: Int,
    onFiltersClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Pretraži menjačnice") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        BadgedBox(badge = {
            if (activeFilters > 0) Badge { Text("$activeFilters") }
        }) {
            FilledTonalIconButton(onClick = onFiltersClick) {
                Icon(Icons.Default.Settings, contentDescription = "Filteri")
            }
        }
    }
}

@Composable
private fun BottomBar(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        "Mapa" to Icons.Default.Place,
        "Tabela" to Icons.AutoMirrored.Filled.List,
        "Rang lista" to Icons.Default.Star
    )
    NavigationBar {
        items.forEachIndexed { index, (label, icon) ->
            NavigationBarItem(
                selected = selected == index,
                onClick = { onSelect(index) },
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun LocationDenied(onAllow: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Za prikaz mape potrebna je dozvola za tačnu lokaciju.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAllow) {
            Text("Dozvoli lokaciju")
        }
    }
}

private fun Filters.activeCount(): Int = listOf(
    authorId != null,
    types.isNotEmpty(),
    minSell != null || maxSell != null,
    minRating > 0,
    fromMillis != null,
    radiusKm != null
).count { it }

private fun hasLocationPermission(context: Context) =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
