package org.samis.whiteboard.domain.repository

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import org.samis.whiteboard.domain.model.ColorPaletteType
import org.samis.whiteboard.domain.model.ColorScheme
import org.samis.whiteboard.presentation.settings.util.DashboardSizeOption
import org.samis.whiteboard.presentation.util.DrawingToolVisibility

interface SettingsRepository {
    fun getColorScheme(): Flow<ColorScheme>
    fun getPreferredStrokeColors(): Flow<List<Color>>
    fun getPreferredMarkerColors(): Flow<List<Color>>
    fun getPreferredFillColors(): Flow<List<Color>>
    fun getPreferredCanvasColors(): Flow<List<Color>>
    fun getDrawingToolVisibility(): Flow<DrawingToolVisibility>
    fun getDashboardSize(): Flow<DashboardSizeOption>
    suspend fun saveColorScheme(colorScheme: ColorScheme)
    suspend fun savePreferredColors(colors: List<Color>, colorPaletteType: ColorPaletteType)
    suspend fun saveDrawingToolVisibility(toolVisibility: DrawingToolVisibility)
    suspend fun saveDashboardSize(size: DashboardSizeOption)
}