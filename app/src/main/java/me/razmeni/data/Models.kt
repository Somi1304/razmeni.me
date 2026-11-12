package me.razmeni.data

import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val id: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val points: Long = 0,
    val lat: Double? = null,
    val lng: Double? = null,
    val lastLocationAt: Timestamp? = null
)

data class Comment(
    val authorUsername: String = "",
    val text: String = "",
    val createdAt: Timestamp? = null
)

data class Exchange(
    @DocumentId val id: String = "",
    val name: String = "",
    val type: String = ExchangeType.MENJACNICA.name,
    val address: String = "",
    val description: String = "",
    val workingHours: String = "",
    val buyEur: Double = 0.0,
    val sellEur: Double = 0.0,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val photoUrl: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val createdAt: Timestamp? = null,
    val lastInteractionAt: Timestamp? = null,
    val ratings: Map<String, Long> = emptyMap(),
    val comments: List<Comment> = emptyList(),
    val visitedBy: List<String> = emptyList()
)

enum class ExchangeType(val label: String, val color: Color) {
    MENJACNICA("Menjačnica", Color(0xFF2E7D32)),
    BANKA("Banka", Color(0xFF1565C0)),
    POSTA("Pošta", Color(0xFFF9A825))
}

enum class DateField { CREATED, LAST_INTERACTION }

enum class SortBy { NAME, SELL_ASC, SELL_DESC, DISTANCE_ASC, DISTANCE_DESC }

data class Filters(
    val query: String = "",
    val authorId: String? = null,
    val types: Set<ExchangeType> = emptySet(),
    val minSell: Double? = null,
    val maxSell: Double? = null,
    val minRating: Int = 0,
    val dateField: DateField = DateField.LAST_INTERACTION,
    val fromMillis: Long? = null,
    val toMillis: Long? = null,
    val radiusKm: Float? = null
)

fun Exchange.exchangeType(): ExchangeType =
    ExchangeType.entries.find { it.name == type } ?: ExchangeType.MENJACNICA

fun Exchange.avgRating(): Double = if (ratings.isEmpty()) 0.0 else ratings.values.average()

fun Exchange.ratingCount(): Int = ratings.size
