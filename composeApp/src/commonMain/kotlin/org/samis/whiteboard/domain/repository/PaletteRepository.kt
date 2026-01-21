package org.samis.whiteboard.domain.repository

import kotlinx.coroutines.flow.Flow
import org.samis.whiteboard.presentation.util.Palette

interface PaletteRepository {
    suspend fun upsertPalette(palette: Palette): Long
    suspend fun deletePalette(palette: Palette)
    fun getAllPalettes(): Flow<List<Palette>>
    suspend fun getPaletteById(paletteId: Long): Palette?
}