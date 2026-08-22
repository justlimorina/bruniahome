package com.example.ui.drawer

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppItem
import com.example.data.model.LauncherSettings
import com.example.ui.components.AppContextMenuDialog
import com.example.ui.components.AppIconItem
import com.example.ui.components.PixelSearchBar

@Composable
fun AppDrawerPage(
    allApps: List<AppItem>,
    settings: LauncherSettings,
    onLaunchApp: (AppItem) -> Unit,
    onTogglePin: (String) -> Unit,
    onHideApp: (String) -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedAppForMenu by remember { mutableStateOf<AppItem?>(null) }

    val voiceSearchPrompt = stringResource(R.string.voice_search_prompt)
    val voiceUnavailableMsg = stringResource(R.string.voice_search_unavailable)
    val lensOpeningMsg = stringResource(R.string.open_google_lens)

    // Voice search speech recognition contract
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spokenText.isNullOrBlank()) {
            searchQuery = spokenText
        }
    }

    val pinnedApps = remember(allApps) {
        allApps.filter { it.isPinned && !it.isHidden }
    }

    val regularApps = remember(allApps) {
        allApps.filter { !it.isPinned && !it.isHidden }
    }

    // Filter apps based on search query & hidden state
    val searchResults = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val q = searchQuery.trim().lowercase()
            allApps.filter { !it.isHidden && (it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)) }
        }
    }

    val isSearching = searchQuery.isNotBlank()
    val hasAppsToShow = if (isSearching) searchResults.isNotEmpty() else (pinnedApps.isNotEmpty() || regularApps.isNotEmpty())

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_drawer_page")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            // Pill-shaped Search Bar at top
            PixelSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onVoiceSearchClick = {
                    try {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_PROMPT, voiceSearchPrompt)
                        }
                        speechRecognizerLauncher.launch(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, voiceUnavailableMsg, Toast.LENGTH_SHORT).show()
                    }
                },
                onLensClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://lens.google.com"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, lensOpeningMsg, Toast.LENGTH_SHORT).show()
                    }
                },
                totalAppsCount = allApps.count { !it.isHidden },
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            // Main Apps Grid
            if (hasAppsToShow) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(settings.appDrawerColumns),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("all_apps_grid"),
                    contentPadding = PaddingValues(bottom = 96.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    if (isSearching) {
                        // Search results mode
                        items(
                            items = searchResults,
                            key = { it.packageName }
                        ) { app ->
                            AppIconItem(
                                app = app,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { selectedAppForMenu = app },
                                showLabel = settings.showAppLabels,
                                iconPackStyle = settings.iconPackStyle,
                                themedIcons = settings.themedIcons,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    } else {
                        // Regular drawer mode: Pinned apps card container (if any), then all apps
                        if (pinnedApps.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Surface(
                                    shape = RoundedCornerShape(26.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    tonalElevation = 2.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                        .testTag("pinned_apps_container")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp, bottom = 12.dp, start = 8.dp, end = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = stringResource(R.string.pinned_apps),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        // Balanced grid rows for pinned apps matching the exact column count and spacing
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            pinnedApps.chunked(settings.appDrawerColumns).forEach { rowApps ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceAround,
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    rowApps.forEach { app ->
                                                        AppIconItem(
                                                            app = app,
                                                            onClick = { onLaunchApp(app) },
                                                            onLongClick = { selectedAppForMenu = app },
                                                            showLabel = settings.showAppLabels,
                                                            iconPackStyle = settings.iconPackStyle,
                                                            themedIcons = settings.themedIcons,
                                                            modifier = Modifier.padding(vertical = 2.dp)
                                                        )
                                                    }
                                                    // Fill remaining empty slots in the row for perfect alignment
                                                    repeat(settings.appDrawerColumns - rowApps.size) {
                                                        Spacer(modifier = Modifier.size(68.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Remaining apps
                        items(
                            items = regularApps,
                            key = { it.packageName }
                        ) { app ->
                            AppIconItem(
                                app = app,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { selectedAppForMenu = app },
                                showLabel = settings.showAppLabels,
                                iconPackStyle = settings.iconPackStyle,
                                themedIcons = settings.themedIcons,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                // Empty search result state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = stringResource(R.string.no_apps_found),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.no_apps_found_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Long press Context Menu Dialog
        selectedAppForMenu?.let { app ->
            AppContextMenuDialog(
                app = app,
                onDismiss = { selectedAppForMenu = null },
                onTogglePin = { onTogglePin(app.packageName) },
                onHideApp = { onHideApp(app.packageName) },
                onAppInfo = { onOpenAppInfo(app.packageName) },
                onUninstall = { onUninstallApp(app.packageName) }
            )
        }
    }
}
