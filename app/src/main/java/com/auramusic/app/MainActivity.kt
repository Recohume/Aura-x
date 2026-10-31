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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.auramusic.app.core.media.AuraMediaService
import com.auramusic.app.ui.AuraViewModel
import com.auramusic.app.ui.screens.AuraMainScreen
import com.auramusic.app.ui.theme.AuraMusicTheme

class MainActivity : ComponentActivity() {
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

        // Request POST_NOTIFICATIONS runtime permission for Android 13+ so media controls appear in notification center
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        try {
            val serviceIntent = Intent(this, AuraMediaService::class.java)
            startService(serviceIntent)
        } catch (_: Exception) {
        }

        setContent {
            AuraMusicTheme {
                val auraViewModel: AuraViewModel = viewModel()
                AuraMainScreen(viewModel = auraViewModel)
            }
        }
    }
}
