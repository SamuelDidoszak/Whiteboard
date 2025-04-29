package org.samis.whiteboard.domain.repository

import kotlinx.coroutines.flow.Flow
import org.samis.whiteboard.domain.model.Update

interface UpdateRepository {
    suspend fun upsertUpdate(update: Update)

    suspend fun deleteUpdate(update: Update)

    fun getWhiteboardUpdates(whiteboardId: Long): Flow<List<Update>>
}