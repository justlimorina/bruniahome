package com.example.data.model

/**
 * Launcher user preferences and theme settings.
 */
data class LauncherSettings(
    val dynamicColor: Boolean = true,
    val themedIcons: Boolean = false,
    val iconPackStyle: IconPackStyle = IconPackStyle.PIXEL_ROUND,
    val appDrawerColumns: Int = 5,
    val showAppLabels: Boolean = true,
    val appIconScale: Float = 1.0f,
    val monetPalette: MonetPalette = MonetPalette.TONAL_SPOT,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val wallpaperPreviewIndex: Int = 0,
    val blurEffectEnabled: Boolean = true
) {
    // Backward-compat aliases
    val dynamicColorEnabled: Boolean get() = dynamicColor
    val themedIconsEnabled: Boolean get() = themedIcons
    val monetPaletteTheme: MonetPalette get() = monetPalette
}

enum class IconPackStyle(val title: String, val shapeName: String) {
    PIXEL_ROUND("Circle", "Circle"),
    SQUIRCLE("Squircle", "Squircle"),
    ROUNDED_SQUARE("Rounded Square", "RoundedSquare"),
    TEARDROP("Teardrop", "Teardrop"),
    SYSTEM_DEFAULT("System Default", "System")
}

enum class AppThemeMode(val title: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class MonetPalette(
    val title: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val tertiaryColorHex: Long,
    val surfaceColorHex: Long
) {
    TONAL_SPOT("Tonal Spot", 0xFF2E6B4F, 0xFF4E6355, 0xFF3D6472, 0xFFF5FAF3),
    VIBRANT("Vibrant", 0xFF006494, 0xFF4F6070, 0xFF63597C, 0xFFF6FAFE),
    EXPRESSIVE("Expressive", 0xFF684FA3, 0xFF635B70, 0xFF7E525D, 0xFFFCF7FF),
    SPRITZ("Spritz", 0xFF41474D, 0xFF5D5F63, 0xFF605E67, 0xFFF1F1F3),
    FRUIT_SALAD("Fruit Salad", 0xFF994326, 0xFF77574D, 0xFF675F30, 0xFFFFF8F6)
}
