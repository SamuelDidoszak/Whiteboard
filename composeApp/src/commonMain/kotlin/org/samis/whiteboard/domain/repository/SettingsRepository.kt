package org.samis.whiteboard.domain.repository

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.Flow
import org.samis.whiteboard.domain.model.ColorPaletteType
import org.samis.whiteboard.domain.model.ColorScheme
import org.samis.whiteboard.presentation.settings.util.DashboardSizeOption
import org.samis.whiteboard.presentation.util.DrawingToolVisibility
import org.samis.whiteboard.presentation.util.Palette

interface SettingsRepository {
    fun getColorScheme(): Flow<ColorScheme>
    fun getPreferredStrokeColors(): Flow<List<Color>>
    fun getPreferredMarkerColors(): Flow<List<Color>>
    fun getPreferredFillColors(): Flow<List<Color>>
    fun getPreferredCanvasColors(): Flow<List<Color>>
    fun getDrawingToolVisibility(): Flow<DrawingToolVisibility>
    fun getDashboardSize(): Flow<DashboardSizeOption>
    fun getStylusInput(): Flow<Boolean>
    fun getShowOpacitySlider(): Flow<Boolean>
    fun getLastPalette(): Flow<Palette>
    fun getMiniatureSaveLocation(): Flow<Boolean>
    fun getAskedForPermissions(): Flow<Boolean>
    suspend fun saveColorScheme(colorScheme: ColorScheme)
    suspend fun savePreferredColors(colors: List<Color>, colorPaletteType: ColorPaletteType)
    suspend fun saveDrawingToolVisibility(toolVisibility: DrawingToolVisibility)
    suspend fun saveDashboardSize(size: DashboardSizeOption)
    suspend fun saveStylusInput(stylusInput: Boolean)
    suspend fun saveShowOpacitySlider(showOpacitySlider: Boolean)
    suspend fun saveLastPalette(palette: Palette)
    suspend fun saveMiniatureSaveLocation(external: Boolean)
    suspend fun saveAskedForPermissions(asked: Boolean)
}