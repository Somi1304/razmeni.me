package me.razmeni.util

import android.location.Location
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

private val serbian = Locale.forLanguageTag("sr-RS")

fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
    val result = FloatArray(1)
    Location.distanceBetween(lat1, lng1, lat2, lng2, result)
    return result[0]
}

fun formatDistance(meters: Float): String =
    if (meters < 1000) "${meters.roundToInt()} m" else "%.1f km".format(serbian, meters / 1000)

fun formatRate(value: Double): String = "%.2f".format(serbian, value)

fun formatRating(value: Double): String = "%.1f".format(serbian, value)

fun formatDate(timestamp: Timestamp?): String =
    if (timestamp == null) "-"
    else SimpleDateFormat("dd.MM.yyyy. HH:mm", serbian).format(timestamp.toDate())
