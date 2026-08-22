package com.example.data.repository

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppManager(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    suspend fun loadInstalledApps(
        pinnedPackages: Set<String>,
        hiddenPackages: Set<String>
    ): List<AppItem> = withContext(Dispatchers.IO) {
        val appList = mutableListOf<AppItem>()
        val seenPackages = mutableSetOf<String>()

        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)

            for (info in resolveInfos) {
                val pkgName = info.activityInfo.packageName
                if (pkgName == context.packageName) {
                    continue // Skip BruniaHome itself
                }
                seenPackages.add(pkgName)

                val label = info.loadLabel(packageManager).toString()
                val icon = try {
                    info.loadIcon(packageManager)
                } catch (_: Exception) {
                    null
                }

                val category = guessCategory(pkgName, label)

                appList.add(
                    AppItem(
                        packageName = pkgName,
                        activityName = info.activityInfo.name,
                        label = label,
                        iconDrawable = icon,
                        fallbackIconName = getFallbackIconTag(label),
                        isPinned = pinnedPackages.contains(pkgName),
                        isHidden = hiddenPackages.contains(pkgName),
                        category = category
                    )
                )
            }
        } catch (_: Exception) {
            // fallback
        }

        // If very few system apps are present in emulator/sandbox, provide Pixel starter apps
        if (appList.size < 8) {
            val defaultPixelApps = getSamplePixelApps(pinnedPackages, hiddenPackages)
            for (app in defaultPixelApps) {
                if (!seenPackages.contains(app.packageName)) {
                    appList.add(app)
                }
            }
        }

        // Sort alphabetically by label
        appList.sortedBy { it.label.lowercase() }
    }

    fun launchApp(app: AppItem) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            } else {
                // Try direct intent for standard actions
                val fallbackIntent = getFallbackIntent(app.packageName)
                if (fallbackIntent != null) {
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallbackIntent)
                } else {
                    Toast.makeText(context, "Mở ứng dụng: ${app.label}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Không thể mở ${app.label}: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Không thể mở Thông tin ứng dụng", Toast.LENGTH_SHORT).show()
        }
    }

    fun uninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                data = Uri.parse("package:$packageName")
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intentFallback = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intentFallback)
            } catch (ex: Exception) {
                Toast.makeText(context, "Không thể gỡ cài đặt: ${ex.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFallbackIntent(packageName: String): Intent? {
        return when {
            packageName.contains("dialer") || packageName.contains("phone") ->
                Intent(Intent.ACTION_DIAL)
            packageName.contains("messaging") || packageName.contains("sms") ->
                Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
            packageName.contains("camera") ->
                Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            packageName.contains("chrome") || packageName.contains("browser") ->
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
            packageName.contains("settings") ->
                Intent(Settings.ACTION_SETTINGS)
            packageName.contains("maps") ->
                Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=Google"))
            else -> null
        }
    }

    private fun guessCategory(packageName: String, label: String): AppCategory {
        val lower = (packageName + " " + label).lowercase()
        return when {
            lower.contains("dialer") || lower.contains("phone") || lower.contains("call") ||
            lower.contains("contact") || lower.contains("messag") || lower.contains("chat") ||
            lower.contains("mail") || lower.contains("tele") || lower.contains("whatsapp") -> AppCategory.COMMUNICATION

            lower.contains("photo") || lower.contains("gallery") || lower.contains("music") ||
            lower.contains("audio") || lower.contains("video") || lower.contains("player") ||
            lower.contains("youtube") || lower.contains("spotify") || lower.contains("camera") ||
            lower.contains("media") -> AppCategory.MEDIA

            lower.contains("doc") || lower.contains("note") || lower.contains("task") ||
            lower.contains("calc") || lower.contains("calendar") || lower.contains("clock") ||
            lower.contains("drive") || lower.contains("file") || lower.contains("browser") ||
            lower.contains("chrome") -> AppCategory.PRODUCTIVITY

            lower.contains("game") || lower.contains("play") || lower.contains("arcade") -> AppCategory.GAMES

            lower.contains("setting") || lower.contains("system") || lower.contains("android") -> AppCategory.SYSTEM

            else -> AppCategory.OTHERS
        }
    }

    private fun getFallbackIconTag(label: String): String {
        val lower = label.lowercase()
        return when {
            lower.contains("điện thoại") || lower.contains("phone") || lower.contains("dialer") -> "phone"
            lower.contains("tin nhắn") || lower.contains("message") || lower.contains("sms") -> "message"
            lower.contains("chrome") || lower.contains("trình duyệt") || lower.contains("browser") -> "browser"
            lower.contains("máy ảnh") || lower.contains("camera") -> "camera"
            lower.contains("ảnh") || lower.contains("photos") || lower.contains("gallery") -> "photos"
            lower.contains("bản đồ") || lower.contains("maps") -> "maps"
            lower.contains("gmail") || lower.contains("mail") || lower.contains("thư") -> "mail"
            lower.contains("cài đặt") || lower.contains("settings") -> "settings"
            lower.contains("đồng hồ") || lower.contains("clock") -> "clock"
            lower.contains("lịch") || lower.contains("calendar") -> "calendar"
            lower.contains("máy tính") || lower.contains("calculator") -> "calculator"
            lower.contains("tệp") || lower.contains("files") -> "files"
            lower.contains("âm nhạc") || lower.contains("spotify") || lower.contains("music") -> "music"
            lower.contains("youtube") -> "youtube"
            lower.contains("ghi chú") || lower.contains("keep") || lower.contains("notes") -> "notes"
            lower.contains("ch play") || lower.contains("store") || lower.contains("cửa hàng") -> "store"
            else -> "default"
        }
    }

    private fun getSamplePixelApps(
        pinnedPackages: Set<String>,
        hiddenPackages: Set<String>
    ): List<AppItem> {
        val sampleList = listOf(
            AppItem(
                packageName = "com.google.android.dialer",
                label = "Điện thoại",
                fallbackIconName = "phone",
                category = AppCategory.COMMUNICATION
            ),
            AppItem(
                packageName = "com.google.android.apps.messaging",
                label = "Tin nhắn",
                fallbackIconName = "message",
                category = AppCategory.COMMUNICATION
            ),
            AppItem(
                packageName = "com.android.chrome",
                label = "Chrome",
                fallbackIconName = "browser",
                category = AppCategory.PRODUCTIVITY
            ),
            AppItem(
                packageName = "com.google.android.GoogleCamera",
                label = "Máy ảnh",
                fallbackIconName = "camera",
                category = AppCategory.MEDIA
            ),
            AppItem(
                packageName = "com.google.android.apps.photos",
                label = "Ảnh",
                fallbackIconName = "photos",
                category = AppCategory.MEDIA
            ),
            AppItem(
                packageName = "com.google.android.apps.maps",
                label = "Bản đồ",
                fallbackIconName = "maps",
                category = AppCategory.PRODUCTIVITY
            ),
            AppItem(
                packageName = "com.google.android.gm",
                label = "Gmail",
                fallbackIconName = "mail",
                category = AppCategory.COMMUNICATION
            ),
            AppItem(
                packageName = "com.android.settings",
                label = "Cài đặt",
                fallbackIconName = "settings",
                category = AppCategory.SYSTEM
            ),
            AppItem(
                packageName = "com.google.android.deskclock",
                label = "Đồng hồ",
                fallbackIconName = "clock",
                category = AppCategory.PRODUCTIVITY
            ),
            AppItem(
                packageName = "com.google.android.calendar",
                label = "Lịch",
                fallbackIconName = "calendar",
                category = AppCategory.PRODUCTIVITY
            ),
            AppItem(
                packageName = "com.google.android.calculator",
                label = "Máy tính",
                fallbackIconName = "calculator",
                category = AppCategory.PRODUCTIVITY
            ),
            AppItem(
                packageName = "com.google.android.apps.nbu.files",
                label = "Tệp",
                fallbackIconName = "files",
                category = AppCategory.PRODUCTIVITY
            ),
            AppItem(
                packageName = "com.spotify.music",
                label = "Spotify",
                fallbackIconName = "music",
                category = AppCategory.MEDIA
            ),
            AppItem(
                packageName = "com.google.android.youtube",
                label = "YouTube",
                fallbackIconName = "youtube",
                category = AppCategory.MEDIA
            ),
            AppItem(
                packageName = "com.google.android.keep",
                label = "Ghi chú Keep",
                fallbackIconName = "notes",
                category = AppCategory.PRODUCTIVITY
            ),
            AppItem(
                packageName = "com.android.vending",
                label = "CH Play",
                fallbackIconName = "store",
                category = AppCategory.SYSTEM
            )
        )

        return sampleList.map { app ->
            app.copy(
                isPinned = pinnedPackages.contains(app.packageName),
                isHidden = hiddenPackages.contains(app.packageName)
            )
        }
    }
}
