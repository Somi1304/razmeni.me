package me.razmeni.ui.main

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import me.razmeni.START_LAT
import me.razmeni.START_LNG
import me.razmeni.data.Exchange
import me.razmeni.data.exchangeType
import me.razmeni.util.formatRate
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(
    exchanges: List<Exchange>,
    location: Location?,
    onExchangeClick: (String) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(20.0)
            controller.setCenter(GeoPoint(START_LAT, START_LNG))
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    LaunchedEffect(location != null) {
        if (location != null) {
            mapView.controller.setCenter(GeoPoint(location.latitude, location.longitude))
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = Modifier.fillMaxSize(),
        update = { map ->
            map.overlays.clear()
            exchanges.forEach { map.overlays.add(exchangeMarker(map, it, onExchangeClick)) }
            if (location != null) map.overlays.add(userMarker(map, location))
            map.invalidate()
        }
    )
}

private fun exchangeMarker(map: MapView, exchange: Exchange, onClick: (String) -> Unit) =
    Marker(map).apply {
        position = GeoPoint(exchange.lat, exchange.lng)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        setIcon(dot(map, exchange.exchangeType().color.toArgb(), 22))
        title = exchange.name
        snippet = "Evro: ${formatRate(exchange.sellEur)}"
        setOnMarkerClickListener { _, _ ->
            onClick(exchange.id)
            true
        }
    }

private fun userMarker(map: MapView, location: Location) =
    Marker(map).apply {
        position = GeoPoint(location.latitude, location.longitude)
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        setIcon(dot(map, Color.parseColor("#1E88E5"), 18))
        title = "Ti si ovde"
        setOnMarkerClickListener { _, _ -> true }
    }

private fun dot(map: MapView, color: Int, sizeDp: Int): GradientDrawable {
    val px = (sizeDp * map.resources.displayMetrics.density).toInt()
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setStroke(px / 6, Color.WHITE)
        setSize(px, px)
    }
}
