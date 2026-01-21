package org.samis.whiteboard.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.samis.whiteboard.data.database.dao.PaletteDao
import org.samis.whiteboard.data.mapper.toPalette
import org.samis.whiteboard.data.mapper.toPaletteEntity
import org.samis.whiteboard.domain.repository.PaletteRepository
import org.samis.whiteboard.presentation.util.Palette

class PaletteRepositoryImpl(
    private val paletteDao: PaletteDao
): PaletteRepository {

    override suspend fun upsertPalette(palette: Palette): Long {
        return paletteDao.upsertPalette(palette.toPaletteEntity())
    }

    override suspend fun deletePalette(palette: Palette) {
        return paletteDao.deletePalette(palette.toPaletteEntity())
    }

    override fun getAllPalettes(): Flow<List<Palette>> {
        return paletteDao.getAllPalettes().map { list ->
            list.map { it.toPalette() }
        }
    }

    override suspend fun getPaletteById(paletteId: Long): Palette? {
        return paletteDao.getPaletteById(paletteId)?.toPalette()
    }
}