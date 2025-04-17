package org.samis.whiteboard.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.samis.whiteboard.domain.model.ColorScheme
import org.samis.whiteboard.domain.repository.SettingsRepository
import org.samis.whiteboard.presentation.util.DrawingToolVisibility

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
): ViewModel() {

    val currentColorScheme: StateFlow<ColorScheme> = settingsRepository.getColorScheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = ColorScheme.SYSTEM_DEFAULT
        )

    fun saveColorScheme(colorScheme: ColorScheme) {
        viewModelScope.launch {
            settingsRepository.saveColorScheme(colorScheme)
        }
    }

    val drawingToolVisibility: StateFlow<DrawingToolVisibility> = settingsRepository.getDrawingToolVisibility()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = DrawingToolVisibility()
        )

    fun saveDrawingToolVisibility(toolVisibility: DrawingToolVisibility) {
        viewModelScope.launch {
            settingsRepository.saveDrawingToolVisibility(toolVisibility)
        }
    }
}