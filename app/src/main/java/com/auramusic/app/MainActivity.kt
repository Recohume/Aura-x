package com.auramusic.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.auramusic.app.core.media.AuraMediaService
import com.auramusic.app.core.notifications.AuraNotificationManager
import com.auramusic.app.ui.AuraViewModel
import com.auramusic.app.ui.screens.AuraMainScreen
import com.auramusic.app.ui.theme.AuraMusicTheme

class MainActivity : ComponentActivity() {
    private val auraViewModel: AuraViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            AuraNotificationManager.setupFcmSubscription(applicationContext)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make Android status bar header visible with bright white icons & time
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false // Forces status bar text and icons (time, wifi, battery) to be white
            isAppearanceLightNavigationBars = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }

        // Always initialize FCM subscription and sync device with Supabase pool on startup
        AuraNotificationManager.setupFcmSubscription(applicationContext)

        // Request POST_NOTIFICATIONS runtime permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Check if opened from an update notification
        handleUpdateIntent(intent)

        try {
            val serviceIntent = Intent(this, AuraMediaService::class.java)
            startService(serviceIntent)
        } catch (_: Exception) {
        }

        setContent {
            AuraMusicTheme {
                AuraMainScreen(viewModel = auraViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleUpdateIntent(intent)
    }

    private fun handleUpdateIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("test_update", false) == true) {
            auraViewModel.triggerTestUpdateModal()
        } else if (intent?.getBooleanExtra(AuraNotificationManager.EXTRA_OPEN_UPDATE, false) == true) {
            auraViewModel.checkForUpdates(force = true)
        }
    }
}
