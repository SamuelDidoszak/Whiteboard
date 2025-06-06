package org.samis.whiteboard.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.samis.whiteboard.domain.model.ColorScheme
import org.samis.whiteboard.presentation.settings.component.ColorSchemeDialog
import org.samis.whiteboard.presentation.util.DrawingToolVisibility
import whiteboard.composeapp.generated.resources.Res
import whiteboard.composeapp.generated.resources.ic_theme

@Composable
fun SettingsScreen(
    onBackIconClick: () -> Unit,
    currentScheme: ColorScheme,
    onThemeSelected: (ColorScheme) -> Unit,
    drawingToolVisibility: DrawingToolVisibility,
    onDrawingToolVisibilityChanged: (DrawingToolVisibility) -> Unit
) {

    var isColorSchemeDialogOpen by rememberSaveable { mutableStateOf(false) }

    ColorSchemeDialog(
        isOpen = isColorSchemeDialogOpen,
        onDismiss = { isColorSchemeDialogOpen = false },
        currentScheme = currentScheme,
        onThemeSelected = onThemeSelected
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SettingsTopBar(onBackIconClick = onBackIconClick)
        LazyColumn(
            modifier = Modifier.fillMaxWidth(fraction = 0.25f)
        ) {
            item {
                ListItem(
                    modifier = Modifier.clickable { isColorSchemeDialogOpen = true },
                    headlineContent = { Text(text = "Color Scheme") },
                    supportingContent = { Text(text = currentScheme.label) },
                    leadingContent = {
                        Icon(
                            modifier = Modifier.size(25.dp),
                            painter = painterResource(Res.drawable.ic_theme),
                            contentDescription = "color scheme"
                        )
                    }
                )
            }
            item {
                SettingsGroup("Drawing tools", drawingToolVisibility.createSettingsList(drawingToolVisibility, onDrawingToolVisibilityChanged))
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(
    modifier: Modifier = Modifier,
    onBackIconClick: () -> Unit
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = "Settings") },
        navigationIcon = {
            IconButton(onClick = onBackIconClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate Back"
                )
            }
        }
    )
}

@Composable
private fun SettingsGroup(title: String, settingsItems: List<SettingsItem>) {
    var isExpanded by remember { mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { isExpanded = !isExpanded },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Icon(
            imageVector = if (isExpanded) Icons.Filled.ArrowDropDown else Icons.AutoMirrored.Filled.ArrowRight,
            contentDescription = if (isExpanded) "Collapse $title" else "Expand $title",
            modifier = Modifier.size(32.dp)
        )
    }
    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(animationSpec = tween(durationMillis = 300)),
        exit = shrinkVertically(animationSpec = tween(durationMillis = 300))
    ) {
        Column(modifier = Modifier.padding(start = 24.dp)) {
            settingsItems.forEach { item ->
                SettingsItemRow(item = item)
            }
        }
    }
}

@Composable
private fun SettingsItemRow(item: SettingsItem) {
    var isChecked = item.initialState

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable(onClick = {
                isChecked = !isChecked
                item.onToggle(isChecked)
            }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = item.text,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = { newValue ->
                isChecked = newValue
                item.onToggle(newValue)
            }
        )
    }
}

private fun DrawingToolVisibility.createSettingsList(
    drawingToolVisibility: DrawingToolVisibility,
    onDrawingToolVisibilityChanged: (DrawingToolVisibility) -> Unit): List<SettingsItem> {
    return this.getAllToolStates().map {
        SettingsItem(
            icon = it.key.res,
            text = it.key.name.lowercase().replaceFirstChar { it.uppercaseChar() },
            initialState = it.value,
            onToggle = { on ->
                onDrawingToolVisibilityChanged(drawingToolVisibility.copy(it.key, on))
            }
        ) }
}

private data class SettingsItem(
    val icon: DrawableResource,
    val text: String,
    val initialState: Boolean,
    val onToggle: (Boolean) -> Unit
)