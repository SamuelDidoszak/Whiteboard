package org.samis.whiteboard.data.repository

import androidx.compose.ui.geometry.Offset
import org.samis.whiteboard.data.database.dao.PathDao
import org.samis.whiteboard.data.database.entity.PathEntity
import org.samis.whiteboard.data.mapper.toPathEntity
import org.samis.whiteboard.domain.model.DrawnPath
import org.samis.whiteboard.domain.repository.PathRepository

class PathRepositoryImpl(
    private val pathDao: PathDao
): PathRepository {

    override suspend fun upsertPath(path: DrawnPath, points: List<Offset>): Long {
        return pathDao.upsertPath(path.toPathEntity(points))
    }

    override suspend fun deletePath(path: DrawnPath) {
        pathDao.deletePath(path.toPathEntity(listOf()))
    }

    override suspend fun getPathById(pathId: Long): PathEntity? {
        return pathDao.getPathById(pathId)
    }
}