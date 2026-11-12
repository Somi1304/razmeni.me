package me.razmeni.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@SuppressLint("MissingPermission")
fun locationFlow(context: Context): Flow<Location> = callbackFlow {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .filter { manager.isProviderEnabled(it) }

    providers.mapNotNull { manager.getLastKnownLocation(it) }
        .maxByOrNull { it.time }
        ?.let { trySend(it) }

    val listener = LocationListener { trySend(it) }
    providers.forEach { provider ->
        manager.requestLocationUpdates(provider, 3000L, 0f, listener, Looper.getMainLooper())
    }

    awaitClose { manager.removeUpdates(listener) }
}
