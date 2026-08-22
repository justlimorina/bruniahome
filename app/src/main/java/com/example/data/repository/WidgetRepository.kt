package com.example.data.repository

import android.appwidget.AppWidgetProviderInfo
import com.example.data.model.WidgetModel
import com.example.data.model.WidgetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class WidgetRepository {

    private val _widgets = MutableStateFlow<List<WidgetModel>>(getDefaultWidgets())
    val widgets: StateFlow<List<WidgetModel>> = _widgets.asStateFlow()

    // Interactive widget data states
    val noteItems = MutableStateFlow(
        listOf(
            "⚡ BruniaHome Material You Launcher",
            "🎨 Monet Dynamic Color extracted from wallpaper",
            "📱 Swipe left for widgets, right for settings"
        )
    )

    val quickTogglesState = MutableStateFlow(
        QuickTogglesData(
            isWifiOn = true,
            isBluetoothOn = true,
            isFlashlightOn = false,
            isDndOn = false,
            isBatterySaverOn = false,
            batteryPercent = 88
        )
    )

    val musicPlaybackState = MutableStateFlow(
        MusicData(
            trackTitle = "Pixel Symphony",
            artist = "Material Sound Design",
            isPlaying = true,
            progress = 0.42f
        )
    )

    fun addWidget(type: WidgetType) {
        val newWidget = WidgetModel(
            id = UUID.randomUUID().toString(),
            isSystemAppWidget = false,
            type = type,
            title = type.defaultTitle,
            spanX = type.minSpanX,
            spanY = type.defaultSpanY,
            position = _widgets.value.size
        )
        _widgets.value = _widgets.value + newWidget
    }

    fun addSystemAppWidget(
        appWidgetId: Int,
        providerInfo: AppWidgetProviderInfo,
        label: String,
        spanX: Int = 5,
        spanY: Int = 2
    ) {
        val newWidget = WidgetModel(
            id = "appwidget_${appWidgetId}_${UUID.randomUUID()}",
            isSystemAppWidget = true,
            appWidgetId = appWidgetId,
            providerPackage = providerInfo.provider.packageName,
            providerClass = providerInfo.provider.className,
            title = label,
            spanX = spanX.coerceIn(1, 5),
            spanY = spanY.coerceIn(1, 4),
            position = _widgets.value.size
        )
        _widgets.value = _widgets.value + newWidget
    }

    fun removeWidget(id: String) {
        _widgets.value = _widgets.value.filter { it.id != id }
    }

    fun updateWidgetSpan(id: String, spanX: Int, spanY: Int) {
        _widgets.value = _widgets.value.map {
            if (it.id == id) {
                it.copy(
                    spanX = spanX.coerceIn(1, 5),
                    spanY = spanY.coerceIn(1, 4)
                )
            } else it
        }
    }

    fun addNote(text: String) {
        if (text.isNotBlank()) {
            noteItems.value = noteItems.value + text.trim()
        }
    }

    fun removeNote(index: Int) {
        val current = noteItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            noteItems.value = current
        }
    }

    fun toggleWifi() {
        quickTogglesState.value = quickTogglesState.value.copy(
            isWifiOn = !quickTogglesState.value.isWifiOn
        )
    }

    fun toggleBluetooth() {
        quickTogglesState.value = quickTogglesState.value.copy(
            isBluetoothOn = !quickTogglesState.value.isBluetoothOn
        )
    }

    fun toggleFlashlight() {
        quickTogglesState.value = quickTogglesState.value.copy(
            isFlashlightOn = !quickTogglesState.value.isFlashlightOn
        )
    }

    fun toggleDnd() {
        quickTogglesState.value = quickTogglesState.value.copy(
            isDndOn = !quickTogglesState.value.isDndOn
        )
    }

    fun toggleBatterySaver() {
        quickTogglesState.value = quickTogglesState.value.copy(
            isBatterySaverOn = !quickTogglesState.value.isBatterySaverOn
        )
    }

    fun togglePlayPause() {
        musicPlaybackState.value = musicPlaybackState.value.copy(
            isPlaying = !musicPlaybackState.value.isPlaying
        )
    }

    fun nextTrack() {
        val tracks = listOf(
            "Pixel Symphony" to "Material Sound Design",
            "Monet Breeze" to "Google Design Lab",
            "Material You Chill" to "Android Acoustics",
            "Morning Coffee & Compose" to "Pixel Vibes"
        )
        val currentIndex = tracks.indexOfFirst { it.first == musicPlaybackState.value.trackTitle }
        val nextIndex = (currentIndex + 1) % tracks.size
        musicPlaybackState.value = musicPlaybackState.value.copy(
            trackTitle = tracks[nextIndex].first,
            artist = tracks[nextIndex].second,
            progress = 0.05f
        )
    }

    private fun getDefaultWidgets(): List<WidgetModel> {
        return listOf(
            WidgetModel(
                id = "widget_at_a_glance",
                isSystemAppWidget = false,
                type = WidgetType.AT_A_GLANCE,
                title = "At a Glance",
                spanX = 5,
                spanY = 2,
                position = 0
            ),
            WidgetModel(
                id = "widget_dynamic_clock",
                isSystemAppWidget = false,
                type = WidgetType.DYNAMIC_CLOCK,
                title = "Material Clock",
                spanX = 5,
                spanY = 2,
                position = 1
            ),
            WidgetModel(
                id = "widget_quick_toggles",
                isSystemAppWidget = false,
                type = WidgetType.QUICK_TOGGLES,
                title = "Quick Toggles",
                spanX = 5,
                spanY = 2,
                position = 2
            ),
            WidgetModel(
                id = "widget_quick_notes",
                isSystemAppWidget = false,
                type = WidgetType.QUICK_NOTES,
                title = "Quick Notes",
                spanX = 5,
                spanY = 2,
                position = 3
            )
        )
    }
}

data class QuickTogglesData(
    val isWifiOn: Boolean,
    val isBluetoothOn: Boolean,
    val isFlashlightOn: Boolean,
    val isDndOn: Boolean,
    val isBatterySaverOn: Boolean,
    val batteryPercent: Int
)

data class MusicData(
    val trackTitle: String,
    val artist: String,
    val isPlaying: Boolean,
    val progress: Float
)
