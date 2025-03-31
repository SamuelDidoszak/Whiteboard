package org.samis.whiteboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.samis.whiteboard.domain.model.ColorScheme
import org.samis.whiteboard.presentation.App
import org.samis.whiteboard.presentation.settings.component.ColorSchemeDialog

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
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