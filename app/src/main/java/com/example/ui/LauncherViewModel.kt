package com.example.ui

import android.app.Application
import android.appwidget.AppWidgetProviderInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppItem
import com.example.data.model.AppThemeMode
import com.example.data.model.IconPackStyle
import com.example.data.model.LauncherSettings
import com.example.data.model.MonetPalette
import com.example.data.model.WidgetType
import com.example.data.repository.AppManager
import com.example.data.repository.AppWidgetGroup
import com.example.data.repository.AppWidgetHostManager
import com.example.data.repository.LauncherPreferencesRepository
import com.example.data.repository.WidgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    val prefsRepo = LauncherPreferencesRepository(application)
    val appManager = AppManager(application)
    val widgetRepo = WidgetRepository()

    val settings: StateFlow<LauncherSettings> = prefsRepo.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LauncherSettings()
    )

    private val _rawApps = MutableStateFlow<List<AppItem>>(emptyList())

    val apps: StateFlow<List<AppItem>> = combine(
        _rawApps,
        prefsRepo.pinnedPackagesFlow,
        prefsRepo.hiddenPackagesFlow
    ) { raw, pinned, hidden ->
        raw.map { app ->
            app.copy(
                isPinned = pinned.contains(app.packageName),
                isHidden = hidden.contains(app.packageName)
            )
        }.sortedBy { it.label.lowercase() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _installedWidgetGroups = MutableStateFlow<List<AppWidgetGroup>>(emptyList())
    val installedWidgetGroups: StateFlow<List<AppWidgetGroup>> = _installedWidgetGroups.asStateFlow()

    init {
        refreshApps()
        loadInstalledWidgets()
    }

    fun refreshApps() {
        viewModelScope.launch {
            val loaded = appManager.loadInstalledApps(emptySet(), emptySet())
            _rawApps.value = loaded
        }
    }

    fun refreshInstalledWidgets() {
        loadInstalledWidgets()
    }

    fun loadInstalledWidgets() {
        viewModelScope.launch {
            val groups = withContext(Dispatchers.IO) {
                AppWidgetHostManager.getInstalledWidgetGroups(getApplication())
            }
            _installedWidgetGroups.value = groups
        }
    }

    fun addSystemWidget(
        providerInfo: AppWidgetProviderInfo,
        label: String,
        spanX: Int = 5,
        spanY: Int = 2
    ) {
        val appWidgetId = AppWidgetHostManager.allocateAppWidgetId(getApplication())
        AppWidgetHostManager.bindAppWidgetIdIfAllowed(getApplication(), appWidgetId, providerInfo)
        widgetRepo.addSystemAppWidget(
            appWidgetId = appWidgetId,
            providerInfo = providerInfo,
            label = label,
            spanX = spanX,
            spanY = spanY
        )
    }

    fun addCompanionWidget(type: WidgetType) {
        widgetRepo.addWidget(type)
    }

    fun removeWidget(id: String, appWidgetId: Int = -1) {
        if (appWidgetId != -1) {
            AppWidgetHostManager.deleteAppWidgetId(getApplication(), appWidgetId)
        }
        widgetRepo.removeWidget(id)
    }

    fun launchApp(app: AppItem) {
        appManager.launchApp(app)
    }

    fun togglePinApp(packageName: String) {
        viewModelScope.launch {
            prefsRepo.togglePinApp(packageName)
        }
    }

    fun hideApp(packageName: String) {
        viewModelScope.launch {
            prefsRepo.setAppHidden(packageName, hidden = true)
        }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            prefsRepo.setAppHidden(packageName, hidden = false)
        }
    }

    fun unhideAllApps() {
        viewModelScope.launch {
            prefsRepo.unhideAllApps()
        }
    }

    fun openAppInfo(packageName: String) {
        appManager.openAppInfo(packageName)
    }

    fun uninstallApp(packageName: String) {
        appManager.uninstallApp(packageName)
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setDynamicColor(enabled) }
    }

    fun updateThemedIcons(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setThemedIcons(enabled) }
    }

    fun updateIconPack(style: IconPackStyle) {
        viewModelScope.launch { prefsRepo.setIconPack(style) }
    }

    fun updateColumns(columns: Int) {
        viewModelScope.launch { prefsRepo.setDrawerColumns(columns) }
    }

    fun updateShowLabels(show: Boolean) {
        viewModelScope.launch { prefsRepo.setShowLabels(show) }
    }

    fun updateMonetPalette(palette: MonetPalette) {
        viewModelScope.launch { prefsRepo.setMonetPalette(palette) }
    }

    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }
}
