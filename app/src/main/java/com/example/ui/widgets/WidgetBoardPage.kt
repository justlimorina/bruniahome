package com.example.ui.widgets

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.model.WidgetModel
import com.example.data.model.WidgetType
import com.example.data.repository.AppWidgetGroup
import com.example.data.repository.AppWidgetHostManager
import com.example.data.repository.WidgetRepository

@Composable
fun WidgetBoardPage(
    widgetRepo: WidgetRepository,
    installedWidgetGroups: List<AppWidgetGroup>,
    onAddSystemWidget: (android.appwidget.AppWidgetProviderInfo, String, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val widgets by widgetRepo.widgets.collectAsState()
    var isEditMode by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("widget_board_page")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp, start = 6.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.widgets),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(
                    onClick = { isEditMode = !isEditMode },
                    modifier = Modifier.testTag("btn_toggle_edit_mode")
                ) {
                    Icon(
                        imageVector = if (isEditMode) Icons.Default.Done else Icons.Default.Edit,
                        contentDescription = if (isEditMode) stringResource(R.string.close) else stringResource(R.string.edit),
                        tint = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Widget List
            if (widgets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(R.string.no_widgets_on_board),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 120.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(widgets, key = { it.id }) { widget ->
                        WidgetContainer(
                            widget = widget,
                            widgetRepo = widgetRepo,
                            isEditMode = isEditMode,
                            onDelete = { widgetRepo.removeWidget(widget.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add Widget
        FloatingActionButton(
            onClick = { showAddSheet = true },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 16.dp)
                .testTag("fab_add_widget")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_widget),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.add_widget),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        // Add Widget Bottom Sheet
        if (showAddSheet) {
            AddWidgetBottomSheet(
                installedGroups = installedWidgetGroups,
                onDismiss = { showAddSheet = false },
                onSelectAppWidget = { providerInfo, label, spanX, spanY ->
                    onAddSystemWidget(providerInfo, label, spanX, spanY)
                },
                onSelectCompanionWidget = { type ->
                    widgetRepo.addWidget(type)
                }
            )
        }
    }
}

@Composable
private fun WidgetContainer(
    widget: WidgetModel,
    widgetRepo: WidgetRepository,
    isEditMode: Boolean,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("widget_container_${widget.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isEditMode) {
                        Modifier
                            .clip(RoundedCornerShape(28.dp))
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(28.dp)
                            )
                    } else Modifier
                )
        ) {
            if (widget.isSystemAppWidget && widget.appWidgetId != -1) {
                // Render System AppWidget
                SystemAppWidgetHostContainer(
                    widget = widget,
                    context = context
                )
            } else {
                // Render Built-in smart companion widget
                when (widget.type) {
                    WidgetType.AT_A_GLANCE -> AtAGlanceWidget()
                    WidgetType.DYNAMIC_CLOCK -> DynamicClockWidget()
                    WidgetType.QUICK_TOGGLES -> QuickTogglesWidget(widgetRepo = widgetRepo)
                    WidgetType.MUSIC_PLAYER -> MusicPlayerWidget(widgetRepo = widgetRepo)
                    WidgetType.QUICK_NOTES -> QuickNotesWidget(widgetRepo = widgetRepo)
                    else -> AtAGlanceWidget()
                }
            }
        }

        // Edit Mode Overlay Controls
        AnimatedVisibility(
            visible = isEditMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .padding(8.dp)
                    .size(36.dp),
                tonalElevation = 6.dp
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_widget),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemAppWidgetHostContainer(
    widget: WidgetModel,
    context: Context
) {
    val providerInfo = remember(widget.providerPackage, widget.providerClass) {
        AppWidgetHostManager.findProviderInfo(context, widget.providerPackage, widget.providerClass)
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
    ) {
        if (providerInfo != null) {
            AndroidView(
                factory = { ctx ->
                    val host = AppWidgetHostManager.getHost(ctx)
                    val hostView = host.createView(ctx, widget.appWidgetId, providerInfo)
                    hostView.layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    hostView
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        } else {
            // Fallback display if provider is not reachable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = widget.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${widget.spanX} × ${widget.spanY} Widget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
