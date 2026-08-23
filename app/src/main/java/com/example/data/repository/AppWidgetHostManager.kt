package com.example.data.repository

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle

const val LAUNCHER_APP_WIDGET_HOST_ID = 2048

data class AppWidgetGroup(
    val appPackageName: String,
    val appLabel: String,
    val appIcon: Drawable?,
    val widgets: List<AppWidgetInfoItem>
)

data class AppWidgetInfoItem(
    val providerInfo: AppWidgetProviderInfo,
    val label: String,
    val minWidth: Int,
    val minHeight: Int,
    val targetCellWidth: Int,
    val targetCellHeight: Int,
    val previewImage: Drawable?,
    val icon: Drawable?
)

object AppWidgetHostManager {

    @Volatile
    private var hostInstance: AppWidgetHost? = null

    fun getHost(context: Context): AppWidgetHost {
        return hostInstance ?: synchronized(this) {
            hostInstance ?: AppWidgetHost(context.applicationContext, LAUNCHER_APP_WIDGET_HOST_ID).also {
                hostInstance = it
            }
        }
    }

    fun startListening(context: Context) {
        try {
            getHost(context).startListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopListening(context: Context) {
        try {
            getHost(context).stopListening()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun allocateAppWidgetId(context: Context): Int {
        return getHost(context).allocateAppWidgetId()
    }

    fun deleteAppWidgetId(context: Context, appWidgetId: Int) {
        try {
            getHost(context).deleteAppWidgetId(appWidgetId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bindAppWidgetIdIfAllowed(
        context: Context,
        appWidgetId: Int,
        providerInfo: AppWidgetProviderInfo,
        options: Bundle? = null
    ): Boolean {
        val manager = AppWidgetManager.getInstance(context) ?: return false
        val bundle = options ?: Bundle().apply {
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, providerInfo.minWidth)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, providerInfo.minHeight)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 500)
            putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 500)
        }
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val bound = manager.bindAppWidgetIdIfAllowed(
                    appWidgetId,
                    providerInfo.profile,
                    providerInfo.provider,
                    bundle
                )
                if (bound) {
                    manager.updateAppWidgetOptions(appWidgetId, bundle)
                }
                bound
            } else {
                manager.bindAppWidgetIdIfAllowed(appWidgetId, providerInfo.provider)
            }
        } catch (e: Exception) {
            false
        }
    }

    fun getInstalledWidgetGroups(context: Context): List<AppWidgetGroup> {
        val manager = AppWidgetManager.getInstance(context) ?: return emptyList()
        val pm = context.packageManager
        val providers = try {
            manager.installedProviders ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val grouped = providers.groupBy { it.provider.packageName }
        val result = mutableListOf<AppWidgetGroup>()

        for ((pkgName, providerList) in grouped) {
            val appLabel = try {
                val appInfo = pm.getApplicationInfo(pkgName, 0)
                pm.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                pkgName
            }

            val appIcon = try {
                pm.getApplicationIcon(pkgName)
            } catch (e: Exception) {
                null
            }

            val widgetItems = providerList.map { info ->
                val label = try {
                    info.loadLabel(pm).ifBlank { appLabel }
                } catch (e: Exception) {
                    appLabel
                }

                val preview = try {
                    info.loadPreviewImage(context, 0)
                } catch (e: Exception) {
                    null
                }

                val icon = try {
                    info.loadIcon(context, 0)
                } catch (e: Exception) {
                    null
                }

                val (cellW, cellH) = estimateCellSpan(info.minWidth, info.minHeight)

                AppWidgetInfoItem(
                    providerInfo = info,
                    label = label,
                    minWidth = info.minWidth,
                    minHeight = info.minHeight,
                    targetCellWidth = cellW,
                    targetCellHeight = cellH,
                    previewImage = preview,
                    icon = icon
                )
            }

            result.add(
                AppWidgetGroup(
                    appPackageName = pkgName,
                    appLabel = appLabel,
                    appIcon = appIcon,
                    widgets = widgetItems
                )
            )
        }

        return result.sortedBy { it.appLabel.lowercase() }
    }

    fun findProviderInfo(context: Context, packageName: String, className: String): AppWidgetProviderInfo? {
        val manager = AppWidgetManager.getInstance(context) ?: return null
        val providers = manager.installedProviders ?: return null
        return providers.firstOrNull {
            it.provider.packageName == packageName && it.provider.className == className
        }
    }

    private fun estimateCellSpan(widthDp: Int, heightDp: Int): Pair<Int, Int> {
        val spanX = ((widthDp + 30) / 70).coerceIn(1, 5)
        val spanY = ((heightDp + 30) / 70).coerceIn(1, 4)
        return spanX to spanY
    }
}
