package org.samis.whiteboard.presentation.whiteboard

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
actual fun HideSystemBars() {
    val activity = LocalContext.current as Activity

    // Hide nav bar on enter
    LaunchedEffect(Unit) {
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    // Restore nav bar on exit
    DisposableEffect(Unit) {
        onDispose {
            val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }
}