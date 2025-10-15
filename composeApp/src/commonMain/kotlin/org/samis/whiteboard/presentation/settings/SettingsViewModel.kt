package org.samis.whiteboard.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.samis.whiteboard.domain.model.ColorScheme
import org.samis.whiteboard.domain.repository.SettingsRepository

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
): ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = combine(
        _state,
        settingsRepository.getColorScheme(),
        settingsRepository.getDrawingToolVisibility(),
        settingsRepository.getDashboardSize(),
        settingsRepository.getStylusInput()
    ) { state, colorScheme, drawingToolVisibility, dashboardSize, stylusInput ->
        state.copy(
            currentScheme = colorScheme,
            drawingToolVisibility = drawingToolVisibility,
            dashboardSize = dashboardSize,
            stylusInput = stylusInput
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
                viewModelScope.launch {
                    settingsRepository.saveStylusInput(event.stylusInput)
                }
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