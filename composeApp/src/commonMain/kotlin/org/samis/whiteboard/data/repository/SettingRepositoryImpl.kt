package org.samis.whiteboard.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.samis.whiteboard.data.util.Constant.CANVAS_COLORS_PREF_KEY
import org.samis.whiteboard.data.util.Constant.COLOR_SCHEME_PREF_KEY
import org.samis.whiteboard.data.util.Constant.DASHBOARD_SIZE_PREF_KEY
import org.samis.whiteboard.data.util.Constant.DRAWING_TOOLS_KEY
import org.samis.whiteboard.data.util.Constant.FILL_COLORS_PREF_KEY
import org.samis.whiteboard.data.util.Constant.MARKER_COLORS_PREF_KEY
import org.samis.whiteboard.data.util.Constant.STROKE_COLORS_PREF_KEY
import org.samis.whiteboard.domain.model.ColorPaletteType
import org.samis.whiteboard.domain.model.ColorScheme
import org.samis.whiteboard.domain.model.DrawingTool
import org.samis.whiteboard.domain.repository.SettingsRepository
import org.samis.whiteboard.presentation.settings.util.DashboardSizeOption
import org.samis.whiteboard.presentation.theme.defaultCanvasColors
import org.samis.whiteboard.presentation.theme.defaultDrawingColors
import org.samis.whiteboard.presentation.util.DrawingToolVisibility

class SettingRepositoryImpl(
    private val prefs: DataStore<Preferences>
): SettingsRepository {

    companion object {
        private val COLOR_SCHEME_KEY = stringPreferencesKey(COLOR_SCHEME_PREF_KEY)
        private val PREFERRED_STROKE_COLORS_KEY = stringPreferencesKey(STROKE_COLORS_PREF_KEY)
        private val PREFERRED_MARKER_COLORS_KEY = stringPreferencesKey(MARKER_COLORS_PREF_KEY)
        private val PREFERRED_FILL_COLORS_KEY = stringPreferencesKey(FILL_COLORS_PREF_KEY)
        private val PREFERRED_CANVAS_COLORS_KEY = stringPreferencesKey(CANVAS_COLORS_PREF_KEY)
        private val PREFERRED_DRAWING_TOOLS_KEY = stringPreferencesKey(DRAWING_TOOLS_KEY)
        private val DASHBOARD_SIZE_KEY = stringPreferencesKey(DASHBOARD_SIZE_PREF_KEY)
    }

    override suspend fun saveColorScheme(colorScheme: ColorScheme) {
        prefs.edit { preferences ->
            preferences[COLOR_SCHEME_KEY] = colorScheme.name
        }
    }

    override fun getColorScheme(): Flow<ColorScheme> {
        return prefs.data.map { preferences ->
            val scheme = preferences[COLOR_SCHEME_KEY] ?: ColorScheme.SYSTEM_DEFAULT.name
            ColorScheme.valueOf(scheme)
        }
    }

    override fun getPreferredStrokeColors(): Flow<List<Color>> {
        return prefs.data.map { preferences ->
            val colorsString = preferences[PREFERRED_STROKE_COLORS_KEY]
            colorsString?.parseColors() ?: defaultDrawingColors
        }
    }

    override fun getPreferredMarkerColors(): Flow<List<Color>> {
        return prefs.data.map { preferences ->
            val colorsString = preferences[PREFERRED_MARKER_COLORS_KEY]
            colorsString?.parseColors() ?: defaultDrawingColors
        }
    }

    override fun getPreferredFillColors(): Flow<List<Color>> {
        return prefs.data.map { preferences ->
            val colorsString = preferences[PREFERRED_FILL_COLORS_KEY]
            colorsString?.parseColors() ?: defaultDrawingColors
        }
    }

    override fun getPreferredCanvasColors(): Flow<List<Color>> {
        return prefs.data.map { preferences ->
            val colorsString = preferences[PREFERRED_CANVAS_COLORS_KEY]
            colorsString?.parseColors() ?: defaultCanvasColors
        }
    }

    override fun getDrawingToolVisibility(): Flow<DrawingToolVisibility> {
        return prefs.data.map { preferences ->
            val toolsString = preferences[PREFERRED_DRAWING_TOOLS_KEY]
            val parsed = toolsString?.parseDrawingToolVisibility()
            if (parsed != null)
                DrawingToolVisibility(parsed)
            else
                DrawingToolVisibility()
        }
    }

    override fun getDashboardSize(): Flow<DashboardSizeOption> {
        return prefs.data.map { preferences ->
            val dashboardSizeName = preferences[DASHBOARD_SIZE_KEY] ?: DashboardSizeOption.MEDIUM.name
            DashboardSizeOption.valueOf(dashboardSizeName)
        }
    }

    override suspend fun savePreferredColors(
        colors: List<Color>,
        colorPaletteType: ColorPaletteType
    ) {
        val colorsString = colors.map { it.toArgb() }.joinToString()
        val key = when(colorPaletteType) {
            ColorPaletteType.CANVAS -> PREFERRED_CANVAS_COLORS_KEY
            ColorPaletteType.STROKE -> PREFERRED_STROKE_COLORS_KEY
            ColorPaletteType.MARKER -> PREFERRED_MARKER_COLORS_KEY
            ColorPaletteType.FILL -> PREFERRED_FILL_COLORS_KEY
        }
        prefs.edit { preference ->
            preference[key] = colorsString
        }
    }

    override suspend fun saveDrawingToolVisibility(toolVisibility: DrawingToolVisibility) {
        val toolVisibilityString = toolVisibility.getAllToolStates()
            .map { it.key.name + ":" + it.value.toString() }.joinToString()
        prefs.edit { preference ->
            preference[PREFERRED_DRAWING_TOOLS_KEY] = toolVisibilityString
        }
    }

    override suspend fun saveDashboardSize(size: DashboardSizeOption) {
        prefs.edit { preference ->
            preference[DASHBOARD_SIZE_KEY] = size.name
        }
    }

    private fun String.parseColors() = this.split(", ").map { it.toInt() }.map { Color(it) }
    private fun String.parseDrawingToolVisibility() = this.split(", ").map {
        val split = it.split(":")
        Pair(DrawingTool.valueOf(split[0]), split[1].toBoolean())
    }.toMap()
}