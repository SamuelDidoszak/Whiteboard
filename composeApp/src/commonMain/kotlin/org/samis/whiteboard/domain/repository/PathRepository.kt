package org.samis.whiteboard.domain.repository

import kotlinx.coroutines.flow.Flow
import org.samis.whiteboard.data.database.entity.PathEntity
import org.samis.whiteboard.domain.model.DrawnPath

interface PathRepository {
    suspend fun upsertPath(path: DrawnPath)
    suspend fun deletePath(path: DrawnPath)
    fun getPathsForWhiteboard(whiteboardId: Long): Flow<List<DrawnPath>>
    suspend fun getPathById(pathId: Long): PathEntity?
}