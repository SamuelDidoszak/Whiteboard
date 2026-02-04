package org.samis.whiteboard.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.samis.whiteboard.presentation.settings.component.ColorSchemeDialog
import org.samis.whiteboard.presentation.settings.util.DashboardSizeOption
import org.samis.whiteboard.presentation.util.DrawingToolVisibility
import whiteboard.composeapp.generated.resources.Res
import whiteboard.composeapp.generated.resources.ic_theme
import whiteboard.composeapp.generated.resources.img_pen
import whiteboard.composeapp.generated.resources.opacity

@Composable
fun SettingsScreen(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onBackIconClick: () -> Unit
) {

    var isColorSchemeDialogOpen by rememberSaveable { mutableStateOf(false) }

    ColorSchemeDialog(
        isOpen = isColorSchemeDialogOpen,
        onDismiss = { isColorSchemeDialogOpen = false },
        currentScheme = state.currentScheme,
        onThemeSelected = { onEvent(SettingsEvent.OnColorSchemeSelected(it)) }
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        SettingsTopBar(onBackIconClick = onBackIconClick)
        DashboardSizeGroup(
            modifier = Modifier.fillMaxWidth(fraction = 0.25f),
            dashboardSize = state.dashboardSize,
            onOptionSelected = { onEvent(SettingsEvent.OnDashboardSizeChanged(it)) }
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(fraction = 0.25f)
        ) {
            item {
                ListItem(
                    modifier = Modifier.clickable { isColorSchemeDialogOpen = true },
                    headlineContent = { Text(text = "Color Scheme") },
                    supportingContent = { Text(text = state.currentScheme.label) },
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
                Box(modifier = Modifier.padding(start = 16.dp)) {
                    SettingsItemRow(
                        SettingsItem(Res.drawable.img_pen, "Stylus input", initialState = state.stylusInput, onToggle = { onEvent(
                            SettingsEvent.OnStylusInputChanged(!state.stylusInput)) })
                    )
                }
            }
            item {
                Box(modifier = Modifier.padding(start = 16.dp)) {
                    SettingsItemRow(
                        SettingsItem(Res.drawable.opacity, "Show opacity slider", initialState = state.showOpacitySlider, onToggle = { onEvent(
                            SettingsEvent.OnShowOpacityChanged(!state.showOpacitySlider)) })
                    )
                }
            }
            item {
                SettingsGroup(
                    title = "Drawing tools",
                    settingsItems = state.drawingToolVisibility.createSettingsList(state.drawingToolVisibility,
                    onDrawingToolVisibilityChanged = { onEvent(SettingsEvent.OnDrawingToolVisibilityChanged(it)) })
                )
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
private fun DashboardSizeGroup(
    modifier: Modifier,
    dashboardSize: DashboardSizeOption,
    onOptionSelected: (DashboardSizeOption) -> Unit,
) {
    println("Current size: $dashboardSize")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Dashboard Size",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DashboardSizeOption.entries.forEachIndexed { index, option ->
                val isSelected = option == dashboardSize
                OutlinedButton(
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    val squareSizePercentage = when (option) {
                        DashboardSizeOption.SMALL -> 0.4f
                        DashboardSizeOption.MEDIUM -> 0.5f
                        DashboardSizeOption.LARGE -> 0.6f
                        DashboardSizeOption.XLARGE -> 0.65f
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(
                                    width = 40.dp * squareSizePercentage,
                                    height = 40.dp * squareSizePercentage * 0.8f
                                )
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
    }
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