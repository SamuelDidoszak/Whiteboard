package org.samis.whiteboard.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.samis.whiteboard.domain.model.ColorScheme
import org.samis.whiteboard.domain.repository.SettingsRepository
import org.samis.whiteboard.presentation.settings.util.DashboardSizeOption
import org.samis.whiteboard.presentation.util.DrawingToolVisibility

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
): ViewModel() {

    private var naSkaleCountdown = 0
    private val _state = MutableStateFlow(SettingsState())
    val state = combine(
        listOf(
            _state,
            settingsRepository.getColorScheme(),
            settingsRepository.getDrawingToolVisibility(),
            settingsRepository.getDashboardSize(),
            settingsRepository.getStylusInput(),
            settingsRepository.getShowOpacitySlider(),
            settingsRepository.getAskedForPermissions(),
            settingsRepository.getMiniatureSaveLocation(),
            settingsRepository.getNaSkaleMode()
        )
    ) { flows ->
        val state = flows[0] as SettingsState
        val colorScheme = flows[1] as ColorScheme
        val drawingToolVisibility = flows[2] as DrawingToolVisibility
        val dashboardSize = flows[3] as DashboardSizeOption
        val stylusInput = flows[4] as Boolean
        val showOpacitySlider = flows[5] as Boolean
        val askedForPermissions = flows[6] as Boolean
        val saveMiniatureToExternal = flows[7] as Boolean
        val naSkaleMode = flows[8] as Boolean
        state.copy(
            currentScheme = colorScheme,
            drawingToolVisibility = drawingToolVisibility,
            dashboardSize = dashboardSize,
            stylusInput = stylusInput,
            showOpacitySlider = showOpacitySlider,
            askedForPermissions = askedForPermissions,
            saveMiniatureToExternal = saveMiniatureToExternal,
            naSkaleMode = naSkaleMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = SettingsState()
    )

    fun onEvent(event: SettingsEvent) {
        when(event) {
            is SettingsEvent.OnColorSchemeSelected -> {
                viewModelScope.launch {
                    settingsRepository.saveColorScheme(event.colorScheme)
                }
            }

            is SettingsEvent.OnDrawingToolVisibilityChanged -> {
                viewModelScope.launch {
                    settingsRepository.saveDrawingToolVisibility(event.drawingToolVisibility)
                }
            }

            is SettingsEvent.OnDashboardSizeChanged -> {
                viewModelScope.launch {
                    settingsRepository.saveDashboardSize(event.dashboardSize)
                }
            }

            is SettingsEvent.OnStylusInputChanged -> {
                if (event.stylusInput &&
                    state.value.dashboardSize == DashboardSizeOption.XLARGE &&
                    state.value.saveMiniatureToExternal) naSkaleCountdown++

                val naSkaleModeOn = state.value.naSkaleMode
                viewModelScope.launch {
                    settingsRepository.saveStylusInput(event.stylusInput)
                    if (event.stylusInput && naSkaleCountdown % 7 == 0) {
                        settingsRepository.saveNaSkaleMode(!naSkaleModeOn)
                        onEvent(SettingsEvent.OnNaSkalePopupVisibilityChanged(true))
                    }
                }
            }

            is SettingsEvent.OnShowOpacityChanged -> {
                viewModelScope.launch {
                    settingsRepository.saveShowOpacitySlider(event.showOpacity)
                }
            }

            is SettingsEvent.OnAskedForPermissionsChanged -> {
                viewModelScope.launch {
                    settingsRepository.saveAskedForPermissions(true)
                }
            }

            is SettingsEvent.OnPicturePermissionChanged -> {
                _state.update { _state.value.copy(grantedExternalStoragePermission = event.granted) }
            }

            is SettingsEvent.OnMiniatureSaveLocationChanged -> {
                val externalStoragePermissionGranted = _state.value.grantedExternalStoragePermission
                val miniatureSaveLocation =
                    if (externalStoragePermissionGranted)
                        event.external
                    else
                        false
                viewModelScope.launch {
                    settingsRepository.saveMiniatureSaveLocation(miniatureSaveLocation)
                }
            }

            is SettingsEvent.OnNaSkalePopupVisibilityChanged -> {
                _state.update { _state.value.copy(isNaSkalePopupVisible = event.visible) }
            }
        }
    }

    val currentColorScheme: StateFlow<ColorScheme> = settingsRepository.getColorScheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = ColorScheme.SYSTEM_DEFAULT
        )
}