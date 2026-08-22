package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.model.AppItem
import com.example.data.model.IconPackStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconItem(
    app: AppItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    isThemedIcon: Boolean = false,
    themedIcons: Boolean = isThemedIcon,
    iconPackStyle: IconPackStyle = IconPackStyle.PIXEL_ROUND,
    iconScale: Float = 1.0f,
    customTextColor: Color? = null
) {
    val haptic = LocalHapticFeedback.current
    val shape = getIconShape(iconPackStyle)
    val useThemed = isThemedIcon || themedIcons

    Column(
        modifier = modifier
            .width(72.dp)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 36.dp),
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .padding(vertical = 6.dp)
            .testTag("app_icon_${app.packageName}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Icon Container
        Box(
            modifier = Modifier
                .size(54.dp)
                .scale(iconScale)
                .clip(shape),
            contentAlignment = Alignment.Center
        ) {
            if (useThemed) {
                ThemedAppIcon(
                    app = app,
                    shape = shape
                )
            } else {
                StandardAppIcon(
                    app = app,
                    shape = shape
                )
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.label,
                style = if (customTextColor != null) {
                    MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = customTextColor
                    )
                } else {
                    TextStyle(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.72f),
                            offset = Offset(0f, 2.5f),
                            blurRadius = 5f
                        )
                    )
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(68.dp)
            )
        }
    }
}

@Composable
private fun ThemedAppIcon(
    app: AppItem,
    shape: Shape
) {
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val iconColor = MaterialTheme.colorScheme.onPrimaryContainer

    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(shape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        if (app.iconDrawable != null) {
            val bitmap = remember(app.iconDrawable) {
                try { app.iconDrawable.toBitmap(128, 128) } catch (e: Exception) { null }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.size(36.dp)
                )
            } else {
                val vector = getAppVectorIcon(app.fallbackIconName)
                Icon(
                    imageVector = vector,
                    contentDescription = app.label,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            val vector = getAppVectorIcon(app.fallbackIconName)
            Icon(
                imageVector = vector,
                contentDescription = app.label,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun StandardAppIcon(
    app: AppItem,
    shape: Shape
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(shape),
        contentAlignment = Alignment.Center
    ) {
        if (app.iconDrawable != null) {
            val bitmap = remember(app.iconDrawable) {
                try { app.iconDrawable.toBitmap(128, 128) } catch (e: Exception) { null }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                FallbackVectorIcon(app = app, shape = shape)
            }
        } else {
            FallbackVectorIcon(app = app, shape = shape)
        }
    }
}

@Composable
private fun FallbackVectorIcon(
    app: AppItem,
    shape: Shape
) {
    val bgColor = remember(app.packageName) {
        val hash = app.packageName.hashCode()
        val colors = listOf(
            Color(0xFF1B6EF3),
            Color(0xFF34A853),
            Color(0xFFEA4335),
            Color(0xFFFBBC04),
            Color(0xFF9C27B0),
            Color(0xFF009688),
            Color(0xFFFF5722),
            Color(0xFF3F51B5)
        )
        colors[Math.abs(hash) % colors.size]
    }

    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(shape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        val vector = getAppVectorIcon(app.fallbackIconName)
        Icon(
            imageVector = vector,
            contentDescription = app.label,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

fun getIconShape(style: IconPackStyle): Shape {
    return when (style) {
        IconPackStyle.PIXEL_ROUND -> CircleShape
        IconPackStyle.SQUIRCLE -> RoundedCornerShape(16.dp)
        IconPackStyle.ROUNDED_SQUARE -> RoundedCornerShape(12.dp)
        IconPackStyle.TEARDROP -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 6.dp)
        IconPackStyle.SYSTEM_DEFAULT -> RoundedCornerShape(16.dp)
    }
}

fun getAppVectorIcon(tag: String): ImageVector {
    return when (tag.lowercase()) {
        "phone" -> Icons.Default.Phone
        "message", "sms" -> Icons.Default.Sms
        "browser", "chrome" -> Icons.Default.Language
        "camera" -> Icons.Default.PhotoCamera
        "photos", "gallery", "image" -> Icons.Default.Image
        "maps", "navigation" -> Icons.Default.Map
        "mail", "email", "gmail" -> Icons.Default.Email
        "settings" -> Icons.Default.Settings
        "clock", "alarm" -> Icons.Default.Schedule
        "calendar" -> Icons.Default.CalendarMonth
        "calculator" -> Icons.Default.Calculate
        "files", "file" -> Icons.Default.Folder
        "music", "audio" -> Icons.Default.Headphones
        "video", "youtube" -> Icons.Default.VideoLibrary
        else -> Icons.Default.Android
    }
}
