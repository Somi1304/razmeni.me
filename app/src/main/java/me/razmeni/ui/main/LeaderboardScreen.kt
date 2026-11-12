package me.razmeni.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.razmeni.data.User
import me.razmeni.ui.common.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(users: List<User>, myUid: String, onLogout: () -> Unit) {
    val ranked = remember(users) { users.sortedByDescending { it.points } }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Rang lista") },
            actions = {
                IconButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Odjavi se")
                }
            }
        )
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(ranked, key = { _, user -> user.id }) { index, user ->
                LeaderboardRow(position = index + 1, user = user, isMe = user.id == myUid)
            }
        }
    }
}

@Composable
private fun LeaderboardRow(position: Int, user: User, isMe: Boolean) {
    val fullName = "${user.firstName} ${user.lastName}"
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isMe) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PositionBadge(position)
            Spacer(Modifier.width(12.dp))
            UserAvatar(image = user.photoUrl, name = fullName, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(user.username, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${user.points}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("poeni", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun PositionBadge(position: Int) {
    val medal = when (position) {
        1 -> Color(0xFFFFC107)
        2 -> Color(0xFFB0BEC5)
        3 -> Color(0xFFD7A26B)
        else -> null
    }
    Box(
        modifier = Modifier
            .size(32.dp)
            .then(if (medal != null) Modifier.background(medal, CircleShape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$position",
            fontWeight = if (medal != null) FontWeight.Bold else FontWeight.Normal,
            color = if (medal != null) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
