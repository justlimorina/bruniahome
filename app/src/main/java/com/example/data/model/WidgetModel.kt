package com.example.data.model

import android.appwidget.AppWidgetProviderInfo

/**
 * Represents a customizable Widget on the 5-column Widget Board.
 */
data class WidgetModel(
    val id: String,
    val isSystemAppWidget: Boolean = false,
    val appWidgetId: Int = -1,
    val providerPackage: String = "",
    val providerClass: String = "",
    val type: WidgetType? = null,
    val title: String,
    val spanX: Int = 5, // Width in 5-column grid (1 to 5)
    val spanY: Int = 2, // Height in rows (1 to 4)
    val position: Int = 0,
    val customData: Map<String, String> = emptyMap()
)

enum class WidgetType(val defaultTitle: String, val minSpanX: Int, val defaultSpanY: Int) {
    AT_A_GLANCE("At a Glance", 5, 2),
    DYNAMIC_CLOCK("Material Clock", 5, 2),
    WEATHER_CARD("Weather", 5, 2),
    MUSIC_PLAYER("Media Player", 5, 2),
    QUICK_TOGGLES("Quick Toggles", 5, 2),
    QUICK_NOTES("Quick Notes", 5, 2),
    BATTERY_STATUS("Battery & Device", 5, 2)
}
