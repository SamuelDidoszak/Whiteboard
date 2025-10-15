package org.samis.whiteboard.presentation.settings

import org.samis.whiteboard.domain.model.ColorScheme
import org.samis.whiteboard.presentation.settings.util.DashboardSizeOption
import org.samis.whiteboard.presentation.util.DrawingToolVisibility

data class SettingsState(
    val currentScheme: ColorScheme = ColorScheme.SYSTEM_DEFAULT,
    val drawingToolVisibility: DrawingToolVisibility = DrawingToolVisibility(),
    val dashboardSize: DashboardSizeOption = DashboardSizeOption.MEDIUM,
    val stylusInput: Boolean = false
)
