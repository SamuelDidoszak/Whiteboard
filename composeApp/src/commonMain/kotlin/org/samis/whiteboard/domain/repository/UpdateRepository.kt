package org.samis.whiteboard.domain.repository

import kotlinx.coroutines.flow.Flow
import org.samis.whiteboard.domain.model.UpdateType

interface UpdateRepository {
    suspend fun upsertUpdate(update: UpdateType)

    suspend fun deleteUpdate(update: UpdateType)

    fun getWhiteboardUpdates(whiteboardId: Long): Flow<List<UpdateType>>
}