package org.samis.whiteboard.data.repository

import org.samis.whiteboard.data.database.dao.PathDao
import org.samis.whiteboard.data.database.entity.PathEntity
import org.samis.whiteboard.data.mapper.toPathEntity
import org.samis.whiteboard.domain.model.DrawnPath
import org.samis.whiteboard.domain.repository.PathRepository

class PathRepositoryImpl(
    private val pathDao: PathDao
): PathRepository {

    override suspend fun upsertPath(path: DrawnPath): Long {
        return pathDao.upsertPath(path.toPathEntity())
    }

    override suspend fun deletePath(path: DrawnPath) {
        pathDao.deletePath(path.toPathEntity())
    }

    override suspend fun getPathById(pathId: Long): PathEntity? {
        return pathDao.getPathById(pathId)
    }
}