package org.samis.whiteboard.domain.repository

import org.samis.whiteboard.data.database.entity.PathEntity
import org.samis.whiteboard.domain.model.DrawnPath

interface PathRepository {
    suspend fun upsertPath(path: DrawnPath): Long
    suspend fun deletePath(path: DrawnPath)
    suspend fun getPathById(pathId: Long): PathEntity?
}