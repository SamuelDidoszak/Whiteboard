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
import org.samis.whiteboard.presentation.settings.util.DashboardSizeOption
import org.samis.whiteboard.presentation.util.DrawingToolVisibility
import org.samis.whiteboard.presentation.util.Palette

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
): ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = combine(
        listOf(
            settingsRepository.getColorScheme(),
            settingsRepository.getDrawingToolVisibility(),
            settingsRepository.getDashboardSize(),
            settingsRepository.getStylusInput(),
            settingsRepository.getShowOpacitySlider(),
            settingsRepository.getLastPalette()
        )
    ) { flows ->
        val colorScheme = flows[0] as ColorScheme
        val drawingToolVisibility = flows[1] as DrawingToolVisibility
        val dashboardSize = flows[2] as DashboardSizeOption
        val stylusInput = flows[3] as Boolean
        val showOpacitySlider = flows[4] as Boolean
        val lastPalette = flows[5] as Palette
        SettingsState(
            currentScheme = colorScheme,
            drawingToolVisibility = drawingToolVisibility,
            dashboardSize = dashboardSize,
            stylusInput = stylusInput,
            showOpacitySlider = showOpacitySlider,
            lastPalette = lastPalette
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

            is SettingsEvent.OnShowOpacityChanged -> {
                viewModelScope.launch {
                    settingsRepository.saveShowOpacitySlider(event.showOpacity)
                }
            }

            is SettingsEvent.OnLastPaletteChanged -> {
                TODO()
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