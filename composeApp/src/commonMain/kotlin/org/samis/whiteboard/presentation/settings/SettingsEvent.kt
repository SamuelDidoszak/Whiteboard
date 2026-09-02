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
    data class OnAskedForPermissionsChanged(val asked: Boolean): SettingsEvent()
    data class OnPicturePermissionChanged(val granted: Boolean): SettingsEvent()
    data class OnMiniatureSaveLocationChanged(val external: Boolean): SettingsEvent()
    data class OnNaSkalePopupVisibilityChanged(val visible: Boolean): SettingsEvent()
}