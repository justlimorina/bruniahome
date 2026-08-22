package com.example.data.model

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents an installed or simulated application for BruniaHome launcher.
 */
data class AppItem(
    val packageName: String,
    val activityName: String = "",
    val label: String,
    val iconDrawable: Drawable? = null,
    val fallbackIconName: String = "default",
    val isPinned: Boolean = false,
    val isHidden: Boolean = false,
    val category: AppCategory = AppCategory.OTHERS,
    val installTime: Long = System.currentTimeMillis()
)

enum class AppCategory(val displayName: String) {
    SYSTEM("Hệ thống"),
    COMMUNICATION("Liên lạc"),
    MEDIA("Giải trí & Phương tiện"),
    PRODUCTIVITY("Công việc & Tiện ích"),
    GAMES("Trò chơi"),
    OTHERS("Khác")
}
