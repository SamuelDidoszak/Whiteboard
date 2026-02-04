package org.samis.whiteboard.presentation.settings

import org.samis.whiteboard.domain.model.ColorScheme
import org.samis.whiteboard.presentation.settings.util.DashboardSizeOption
import org.samis.whiteboard.presentation.util.DrawingToolVisibility

sealed class SettingsEvent {
    data class OnColorSchemeSelected(val colorScheme: ColorScheme) : SettingsEvent()
    data class OnDrawingToolVisibilityChanged(val drawingToolVisibility: DrawingToolVisibility): SettingsEvent()
    data class OnDashboardSizeChanged(val dashboardSize: DashboardSizeOption): SettingsEvent()
    data class OnStylusInputChanged(val stylusInput: Boolean): SettingsEvent()
    data class OnShowOpacityChanged(val showOpacity: Boolean): SettingsEvent()
    data class OnLastPaletteChanged(val lastPalette: Boolean): SettingsEvent()
}