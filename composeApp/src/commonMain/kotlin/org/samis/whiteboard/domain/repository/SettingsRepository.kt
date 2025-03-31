package org.samis.whiteboard.domain.repository

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import org.samis.whiteboard.domain.model.ColorPaletteType
import org.samis.whiteboard.domain.model.ColorScheme

interface SettingsRepository {
    fun getColorScheme(): Flow<ColorScheme>
    fun getPreferredStrokeColors(): Flow<List<Color>>
    fun getPreferredFillColors(): Flow<List<Color>>
    fun getPreferredCanvasColors(): Flow<List<Color>>
    suspend fun saveColorScheme(colorScheme: ColorScheme)
    suspend fun savePreferredColors(colors: List<Color>, colorPaletteType: ColorPaletteType)
}