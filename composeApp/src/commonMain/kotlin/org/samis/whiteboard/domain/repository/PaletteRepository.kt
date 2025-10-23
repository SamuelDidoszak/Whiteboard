package org.samis.whiteboard.domain.repository

import org.samis.whiteboard.presentation.util.Palette

interface PaletteRepository {
    suspend fun upsertPalette(palette: Palette): Long
    suspend fun deletePalette(palette: Palette)
    suspend fun getPaletteById(paletteId: Long): Palette?
}