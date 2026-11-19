package me.razmeni.ui.main

import android.app.Application
import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.razmeni.ACTIVE_USER_MS
import me.razmeni.FORGET_NOTIFIED_M
import me.razmeni.LOCATION_SEND_MS
import me.razmeni.NEARBY_EXCHANGE_M
import me.razmeni.NEARBY_USER_M
import me.razmeni.POINTS_ADD
import me.razmeni.POINTS_COMMENT
import me.razmeni.POINTS_FIRST_RATING
import me.razmeni.POINTS_RATE_UPDATE
import me.razmeni.POINTS_VISIT
import me.razmeni.data.AuthRepo
import me.razmeni.data.DateField
import me.razmeni.data.Exchange
import me.razmeni.data.ExchangeRepo
import me.razmeni.data.Filters
import me.razmeni.data.ImageUploader
import me.razmeni.data.SortBy
import me.razmeni.data.User
import me.razmeni.data.UserRepo
import me.razmeni.data.avgRating
import me.razmeni.data.exchangeType
import me.razmeni.data.seedDemoData
import me.razmeni.location.locationFlow
import me.razmeni.notifications.Notifier
import me.razmeni.util.distanceMeters
import me.razmeni.util.formatRate
import java.util.TimeZone

private const val DAY_MS = 24 * 60 * 60 * 1000L

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val _exchanges = MutableStateFlow<List<Exchange>>(emptyList())
    val exchanges: StateFlow<List<Exchange>> = _exchanges

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location

    private val _filters = MutableStateFlow(Filters())
    val filters: StateFlow<Filters> = _filters

    private val _sort = MutableStateFlow(SortBy.NAME)
    val sort: StateFlow<SortBy> = _sort

    val visibleExchanges: StateFlow<List<Exchange>> =
        combine(_exchanges, _filters, _location, _sort) { list, filters, location, sort ->
            sortExchanges(list.filter { matches(it, filters, location) }, sort, location)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    var busy by mutableStateOf(false)
        private set

    private var listenJob: Job? = null
    private var trackingJob: Job? = null
    private var lastSentAt = 0L
    private val notified = mutableSetOf<String>()

    fun start() {
        if (listenJob?.isActive == true) return
        listenJob = viewModelScope.launch {
            launch {
                ExchangeRepo.listen()
                    .catch { Log.e("MainViewModel", "exchanges", it) }
                    .collect { _exchanges.value = it }
            }
            launch {
                UserRepo.listenAll()
                    .catch { Log.e("MainViewModel", "users", it) }
                    .collect { _users.value = it }
            }
        }
    }

    fun startTracking() {
        if (trackingJob?.isActive == true) return
        trackingJob = viewModelScope.launch {
            locationFlow(getApplication<Application>())
                .catch { Log.e("MainViewModel", "location", it) }
                .collect { onLocation(it) }
        }
    }

    private fun onLocation(location: Location) {
        _location.value = location
        val now = System.currentTimeMillis()
        if (now - lastSentAt >= LOCATION_SEND_MS) {
            lastSentAt = now
            viewModelScope.launch {
                try {
                    UserRepo.updateLocation(location.latitude, location.longitude)
                } catch (e: Exception) {
                    Log.e("MainViewModel", "updateLocation", e)
                }
            }
        }
        checkNearby(location)
    }

    private fun checkNearby(me: Location) {
        val context = getApplication<Application>()
        _exchanges.value.forEach { e ->
            val key = "ex_${e.id}"
            val distance = me.metersTo(e)
            if (distance < NEARBY_EXCHANGE_M && notified.add(key)) {
                Notifier.show(context, key, "Blizu si: ${e.name}", "Evro: ${formatRate(e.sellEur)} din")
            } else if (distance > FORGET_NOTIFIED_M) {
                notified.remove(key)
            }
        }

        val activeSince = System.currentTimeMillis() - ACTIVE_USER_MS
        _users.value.forEach { u ->
            val lat = u.lat
            val lng = u.lng
            if (u.id == AuthRepo.currentUid || lat == null || lng == null) return@forEach
            val key = "us_${u.id}"
            val distance = distanceMeters(me.latitude, me.longitude, lat, lng)
            val active = (u.lastLocationAt?.toDate()?.time ?: 0L) > activeSince
            if (active && distance < NEARBY_USER_M && notified.add(key)) {
                Notifier.show(context, key, "Korisnik u blizini", "${u.username} je na manje od $NEARBY_USER_M m")
            } else if (distance > FORGET_NOTIFIED_M) {
                notified.remove(key)
            }
        }
    }

    fun logout() {
        listenJob?.cancel()
        trackingJob?.cancel()
        _exchanges.value = emptyList()
        _users.value = emptyList()
        _location.value = null
        _filters.value = Filters()
        _sort.value = SortBy.NAME
        notified.clear()
        lastSentAt = 0L
        AuthRepo.logout()
    }

    fun setQuery(query: String) {
        _filters.value = _filters.value.copy(query = query)
    }

    fun applyFilters(filters: Filters) {
        _filters.value = filters
    }

    fun sortBySell() {
        _sort.value = if (_sort.value == SortBy.SELL_ASC) SortBy.SELL_DESC else SortBy.SELL_ASC
    }

    fun sortByDistance() {
        _sort.value = if (_sort.value == SortBy.DISTANCE_ASC) SortBy.DISTANCE_DESC else SortBy.DISTANCE_ASC
    }

    private fun matches(e: Exchange, f: Filters, location: Location?): Boolean {
        val query = f.query.trim()
        val textOk = query.isEmpty() ||
            listOf(e.name, e.address, e.description, e.exchangeType().label)
                .any { it.contains(query, ignoreCase = true) }
        val radiusOk = f.radiusKm == null || location == null ||
            location.metersTo(e) <= f.radiusKm * 1000
        return textOk && radiusOk &&
            (f.authorId == null || e.authorId == f.authorId) &&
            (f.types.isEmpty() || e.exchangeType() in f.types) &&
            (f.minSell == null || e.sellEur >= f.minSell) &&
            (f.maxSell == null || e.sellEur <= f.maxSell) &&
            e.avgRating() >= f.minRating &&
            matchesDate(e, f)
    }

    private fun matchesDate(e: Exchange, f: Filters): Boolean {
        if (f.fromMillis == null && f.toMillis == null) return true
        val timestamp = if (f.dateField == DateField.CREATED) e.createdAt else e.lastInteractionAt
        val millis = timestamp?.toDate()?.time ?: return false
        // DateRangePicker vraća ponoć po UTC-u, pa i lokalno vreme prebacujemo u taj oblik
        val day = millis + TimeZone.getDefault().getOffset(millis)
        return (f.fromMillis == null || day >= f.fromMillis) &&
            (f.toMillis == null || day < f.toMillis + DAY_MS)
    }

    private fun sortExchanges(list: List<Exchange>, sort: SortBy, location: Location?) = when (sort) {
        SortBy.NAME -> list.sortedBy { it.name.lowercase() }
        SortBy.SELL_ASC -> list.sortedBy { it.sellEur }
        SortBy.SELL_DESC -> list.sortedByDescending { it.sellEur }
        SortBy.DISTANCE_ASC -> list.sortedBy { location?.metersTo(it) }
        SortBy.DISTANCE_DESC -> list.sortedByDescending { location?.metersTo(it) }
    }

    fun addExchange(exchange: Exchange, photo: Bitmap?, onDone: () -> Unit) {
        act("Dodato, +$POINTS_ADD poena", onDone) {
            val photoUrl = photo?.let { ImageUploader.upload(it) }.orEmpty()
            ExchangeRepo.add(exchange.copy(photoUrl = photoUrl))
        }
    }

    fun updateRate(exchange: Exchange, buy: Double, sell: Double) {
        act("Kurs ažuriran, +$POINTS_RATE_UPDATE poena") {
            ExchangeRepo.interact(exchange.id, mapOf("buyEur" to buy, "sellEur" to sell), POINTS_RATE_UPDATE)
        }
    }

    fun rate(exchange: Exchange, stars: Int) {
        val uid = AuthRepo.currentUid ?: return
        val first = uid !in exchange.ratings
        val points = if (first) POINTS_FIRST_RATING else 0L
        act(if (first) "Ocena sačuvana, +$points poena" else "Ocena izmenjena, bez novih poena") {
            ExchangeRepo.interact(exchange.id, mapOf("ratings.$uid" to stars.toLong()), points)
        }
    }

    fun comment(exchange: Exchange, text: String) {
        act("Komentar dodat, +$POINTS_COMMENT poena") {
            ExchangeRepo.addComment(exchange.id, text.trim())
        }
    }

    fun visit(exchange: Exchange) {
        val uid = AuthRepo.currentUid ?: return
        act("Poseta potvrđena, +$POINTS_VISIT poena") {
            ExchangeRepo.interact(exchange.id, mapOf("visitedBy" to FieldValue.arrayUnion(uid)), POINTS_VISIT)
        }
    }

    fun seedDemo() {
        act("Ubačene su demo menjačnice") { seedDemoData() }
    }

    fun deleteAllExchanges() {
        act("Obrisane su sve menjačnice") { ExchangeRepo.deleteAll() }
    }

    private fun act(message: String, onDone: () -> Unit = {}, block: suspend () -> Unit) {
        viewModelScope.launch {
            busy = true
            try {
                block()
                toast(message)
                onDone()
            } catch (e: Exception) {
                Log.e("MainViewModel", "action", e)
                toast("Došlo je do greške, pokušaj ponovo")
            }
            busy = false
        }
    }

    private fun toast(text: String) {
        Toast.makeText(getApplication<Application>(), text, Toast.LENGTH_SHORT).show()
    }
}

private fun Location.metersTo(e: Exchange) = distanceMeters(latitude, longitude, e.lat, e.lng)
