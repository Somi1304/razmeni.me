package me.razmeni.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import me.razmeni.data.ExchangeType
import me.razmeni.ui.theme.StarColor

@Composable
fun RatingStars(
    rating: Int,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    onRate: ((Int) -> Unit)? = null
) {
    Row(modifier) {
        for (star in 1..5) {
            val clickable = if (onRate != null) Modifier.clickable { onRate(star) } else Modifier
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = if (onRate != null) "Ocena $star" else null,
                tint = if (star <= rating) StarColor else MaterialTheme.colorScheme.outlineVariant,
                modifier = clickable.size(size)
            )
        }
    }
}

@Composable
fun TypeBadge(type: ExchangeType, modifier: Modifier = Modifier) {
    Text(
        text = type.label,
        color = if (type == ExchangeType.POSTA) Color.Black else Color.White,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        modifier = modifier
            .background(type.color, CircleShape)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun UserAvatar(image: Any?, name: String, size: Dp = 40.dp) {
    val modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer)
    if (image != null && image != "") {
        AsyncImage(
            model = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                text = initials(name),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = (size.value * 0.4f).sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun initials(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}
