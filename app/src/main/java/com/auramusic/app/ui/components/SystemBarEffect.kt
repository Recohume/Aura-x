package com.auramusic.app.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

/**
 * Ensures the Android status bar header (time, battery, wifi, notifications)
 * ALWAYS stays bright white, and never turns dark/black when modals, bottom sheets,
 * or dialogs are opened.
 */
@Composable
fun ForceWhiteSystemBarsEffect() {
    val view = LocalView.current

    DisposableEffect(view) {
        fun applyWhiteBars(win: Window) {
            WindowCompat.getInsetsController(win, win.decorView).apply {
                isAppearanceLightStatusBars = false // false = WHITE text & icons
                isAppearanceLightNavigationBars = false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                win.isStatusBarContrastEnforced = false
                win.isNavigationBarContrastEnforced = false
            }
        }

        // 1. Check if this view belongs to a Dialog / ModalBottomSheet window
        var parent = view.parent
        var dialogWindow: Window? = null
        while (parent != null) {
            if (parent is DialogWindowProvider) {
                dialogWindow = parent.window
                break
            }
            parent = parent.parent
        }

        // 2. Resolve host Activity window
        var context = view.context
        var activity: Activity? = null
        while (context is ContextWrapper) {
            if (context is Activity) {
                activity = context
                break
            }
            context = context.baseContext
        }

        dialogWindow?.let { applyWhiteBars(it) }
        activity?.window?.let { applyWhiteBars(it) }

        onDispose {
            activity?.window?.let { applyWhiteBars(it) }
        }
    }
}
