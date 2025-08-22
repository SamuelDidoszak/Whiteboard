package org.samis.whiteboard

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.samis.whiteboard.domain.model.ColorScheme
import org.samis.whiteboard.presentation.App
import org.samis.whiteboard.presentation.settings.component.ColorSchemeDialog

class MainActivity : ComponentActivity() {

    private val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()
        requestPermissions()

        setContent {
            App()
        }
    }

    private fun ComponentActivity.hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, STORAGE_PERMISSIONS, 0)
    }
}


@Preview
@Composable
private fun Prev() {
    ColorSchemeDialog(
        isOpen = true,
        onDismiss = {},
        currentScheme = ColorScheme.SYSTEM_DEFAULT,
        onThemeSelected = {}
    )
}