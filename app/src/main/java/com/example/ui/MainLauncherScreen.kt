package com.example.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.drawer.AppDrawerPage
import com.example.ui.settings.LauncherSettingsPage
import com.example.ui.widgets.WidgetBoardPage
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainLauncherScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val allApps by viewModel.apps.collectAsState()
    val installedWidgetGroups by viewModel.installedWidgetGroups.collectAsState()

    // 3 Pages: 0 = Widgets, 1 = App Drawer (Default!), 2 = Settings
    val pagerState = rememberPagerState(
        initialPage = 1,
        pageCount = { 3 }
    )
    val coroutineScope = rememberCoroutineScope()

    // Calculate smart background alpha dynamically:
    // Page 0 (Widget): Semi-transparent with ~80% surface scrim overlay
    // Page 1 (App Drawer): Transparent background
    // Page 2 (Settings): Solid/clean surface
    val continuousPosition by remember {
        derivedStateOf {
            pagerState.currentPage + pagerState.currentPageOffsetFraction
        }
    }

    val surfaceColor = MaterialTheme.colorScheme.surface

    val currentScrimColor = remember(continuousPosition, surfaceColor) {
        val pos = continuousPosition.coerceIn(0f, 2f)
        val alpha = when {
            // Page 0 (Widget: 0.80f) -> Page 1 (Drawer: 0.08f)
            pos <= 1f -> {
                val fraction = pos // 0.0 -> 1.0
                0.80f - (0.72f * fraction) // 0.80f -> 0.08f
            }
            // Page 1 (Drawer: 0.08f) -> Page 2 (Settings: 0.96f)
            else -> {
                val fraction = pos - 1f // 0.0 -> 1.0
                0.08f + (0.88f * fraction) // 0.08f -> 0.96f
            }
        }.coerceIn(0.05f, 0.98f)
        surfaceColor.copy(alpha = alpha)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_launcher_root")
    ) {
        // Aesthetic Wallpaper Backdrop layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                        )
                    )
                )
        )

        // Dynamic Scrim Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(currentScrimColor)
        )

        // Horizontal Pager for 3 pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            pageSpacing = 16.dp,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapAnimationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        ) { pageIndex ->
            when (pageIndex) {
                0 -> {
                    // Left Page: Widget Board
                    WidgetBoardPage(
                        widgetRepo = viewModel.widgetRepo,
                        installedWidgetGroups = installedWidgetGroups,
                        onAddSystemWidget = { providerInfo, label, spanX, spanY ->
                            viewModel.addSystemWidget(providerInfo, label, spanX, spanY)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                1 -> {
                    // Middle Page: App Drawer (Default Home)
                    AppDrawerPage(
                        allApps = allApps,
                        settings = settings,
                        onLaunchApp = { viewModel.launchApp(it) },
                        onTogglePin = { viewModel.togglePinApp(it) },
                        onHideApp = { viewModel.hideApp(it) },
                        onOpenAppInfo = { viewModel.openAppInfo(it) },
                        onUninstallApp = { viewModel.uninstallApp(it) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                2 -> {
                    // Right Page: Launcher Settings
                    LauncherSettingsPage(
                        settings = settings,
                        allApps = allApps,
                        onUpdateDynamicColor = { viewModel.updateDynamicColor(it) },
                        onUpdateThemedIcons = { viewModel.updateThemedIcons(it) },
                        onUpdateIconPack = { viewModel.updateIconPack(it) },
                        onUpdateColumns = { viewModel.updateColumns(it) },
                        onUpdateShowLabels = { viewModel.updateShowLabels(it) },
                        onUpdateMonetPalette = { viewModel.updateMonetPalette(it) },
                        onUpdateThemeMode = { viewModel.updateThemeMode(it) },
                        onUnhideApp = { viewModel.unhideApp(it) },
                        onUnhideAll = { viewModel.unhideAllApps() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Bottom Navigation Pill Bar (Edge-to-edge safe)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 12.dp)
        ) {
            LauncherNavigationPill(
                currentPage = pagerState.currentPage,
                onNavigateToPage = { page ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            page = page,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun LauncherNavigationPill(
    currentPage: Int,
    onNavigateToPage: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.90f),
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(52.dp)
            .testTag("launcher_navigation_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NavPillItem(
                icon = Icons.Default.Widgets,
                label = stringResource(R.string.nav_tab_widgets),
                isSelected = currentPage == 0,
                onClick = { onNavigateToPage(0) },
                tag = "nav_tab_widgets"
            )

            NavPillItem(
                icon = Icons.Default.Apps,
                label = stringResource(R.string.nav_tab_apps),
                isSelected = currentPage == 1,
                onClick = { onNavigateToPage(1) },
                tag = "nav_tab_apps"
            )

            NavPillItem(
                icon = Icons.Default.Settings,
                label = stringResource(R.string.nav_tab_settings),
                isSelected = currentPage == 2,
                onClick = { onNavigateToPage(2) },
                tag = "nav_tab_settings"
            )
        }
    }
}

@Composable
private fun NavPillItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            if (isSelected) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
