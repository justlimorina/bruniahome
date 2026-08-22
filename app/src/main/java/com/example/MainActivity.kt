package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.data.repository.AppWidgetHostManager
import com.example.ui.LauncherViewModel
import com.example.ui.MainLauncherScreen
import com.example.ui.theme.BruniaHomeTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()

            BruniaHomeTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor,
                monetPalette = settings.monetPalette
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    MainLauncherScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        AppWidgetHostManager.startListening(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshApps()
        viewModel.refreshInstalledWidgets()
    }

    override fun onStop() {
        super.onStop()
        AppWidgetHostManager.stopListening(this)
    }
}
