package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppThemeMode
import com.example.data.model.IconPackStyle
import com.example.data.model.LauncherSettings
import com.example.data.model.MonetPalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "brunia_launcher_prefs")

class LauncherPreferencesRepository(private val context: Context) {

    private object Keys {
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val THEMED_ICONS = booleanPreferencesKey("themed_icons")
        val ICON_PACK = stringPreferencesKey("icon_pack")
        val DRAWER_COLUMNS = intPreferencesKey("drawer_columns")
        val SHOW_LABELS = booleanPreferencesKey("show_labels")
        val ICON_SCALE = floatPreferencesKey("icon_scale")
        val MONET_PALETTE = stringPreferencesKey("monet_palette")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WALLPAPER_INDEX = intPreferencesKey("wallpaper_index")
        val BLUR_EFFECT = booleanPreferencesKey("blur_effect")
        val PINNED_PACKAGES = stringSetPreferencesKey("pinned_packages")
        val HIDDEN_PACKAGES = stringSetPreferencesKey("hidden_packages")
    }

    val settingsFlow: Flow<LauncherSettings> = context.dataStore.data.map { prefs ->
        val iconPackStr = prefs[Keys.ICON_PACK] ?: IconPackStyle.PIXEL_ROUND.name
        val monetStr = prefs[Keys.MONET_PALETTE] ?: MonetPalette.TONAL_SPOT.name
        val themeStr = prefs[Keys.THEME_MODE] ?: AppThemeMode.SYSTEM.name

        LauncherSettings(
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            themedIcons = prefs[Keys.THEMED_ICONS] ?: false,
            iconPackStyle = try { IconPackStyle.valueOf(iconPackStr) } catch (_: Exception) { IconPackStyle.PIXEL_ROUND },
            appDrawerColumns = prefs[Keys.DRAWER_COLUMNS] ?: 5,
            showAppLabels = prefs[Keys.SHOW_LABELS] ?: true,
            appIconScale = prefs[Keys.ICON_SCALE] ?: 1.0f,
            monetPalette = try { MonetPalette.valueOf(monetStr) } catch (_: Exception) { MonetPalette.TONAL_SPOT },
            themeMode = try { AppThemeMode.valueOf(themeStr) } catch (_: Exception) { AppThemeMode.SYSTEM },
            wallpaperPreviewIndex = prefs[Keys.WALLPAPER_INDEX] ?: 0,
            blurEffectEnabled = prefs[Keys.BLUR_EFFECT] ?: true
        )
    }

    val pinnedPackagesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.PINNED_PACKAGES] ?: emptySet()
    }

    val hiddenPackagesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.HIDDEN_PACKAGES] ?: emptySet()
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setThemedIcons(enabled: Boolean) {
        context.dataStore.edit { it[Keys.THEMED_ICONS] = enabled }
    }

    suspend fun setIconPack(style: IconPackStyle) {
        context.dataStore.edit { it[Keys.ICON_PACK] = style.name }
    }

    suspend fun setDrawerColumns(columns: Int) {
        context.dataStore.edit { it[Keys.DRAWER_COLUMNS] = columns }
    }

    suspend fun setShowLabels(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_LABELS] = show }
    }

    suspend fun setMonetPalette(palette: MonetPalette) {
        context.dataStore.edit { it[Keys.MONET_PALETTE] = palette.name }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun togglePinApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.PINNED_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            prefs[Keys.PINNED_PACKAGES] = current
        }
    }

    suspend fun setAppHidden(packageName: String, hidden: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.HIDDEN_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (hidden) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            prefs[Keys.HIDDEN_PACKAGES] = current
        }
    }

    suspend fun unhideAllApps() {
        context.dataStore.edit { prefs ->
            prefs[Keys.HIDDEN_PACKAGES] = emptySet()
        }
    }
}
