package com.debarunlahiri.clippy

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.debarunlahiri.clippy.service.ClipboardMonitorService
import com.debarunlahiri.clippy.ui.navigation.Screen
import com.debarunlahiri.clippy.ui.screens.history.ClipboardHistoryScreen
import com.debarunlahiri.clippy.ui.screens.settings.SettingsScreen
import com.debarunlahiri.clippy.ui.theme.ClippyTheme

class MainActivity : ComponentActivity() {

    // Permission launcher for POST_NOTIFICATIONS
    private val notificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    startClipboardService()
                } else {
                    Toast.makeText(
                                    this,
                                    "Notification permission is required for the service to run",
                                    Toast.LENGTH_LONG
                            )
                            .show()
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission and start service
        requestNotificationPermissionAndStartService()

        setContent {
            ClippyTheme {
                Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                ) { ClippyApp() }
            }
        }
    }

    /** Request notification permission (Android 13+) and start the service */
    private fun requestNotificationPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    startClipboardService()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            startClipboardService()
        }
    }

    /** Start the clipboard monitoring service */
    private fun startClipboardService() {
        ClipboardMonitorService.start(this)
    }
}

/** Main app composable with navigation */
@Composable
fun ClippyApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        // Home screen (clipboard history)
        composable(Screen.Home.route) {
            ClipboardHistoryScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToDetail = { itemId ->
                        navController.navigate(Screen.ItemDetail.createRoute(itemId))
                    }
            )
        }

        // Settings screen
        composable(Screen.Settings.route) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Item detail screen (placeholder for now)
        composable(
                route = Screen.ItemDetail.route,
                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
            // TODO: Implement item detail screen
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
