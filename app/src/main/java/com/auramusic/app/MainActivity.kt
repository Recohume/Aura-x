package com.auramusic.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.auramusic.app.core.media.AuraMediaService
import com.auramusic.app.ui.AuraViewModel
import com.auramusic.app.ui.screens.AuraMainScreen
import com.auramusic.app.ui.theme.AuraMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            startService(Intent(this, AuraMediaService::class.java))
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
