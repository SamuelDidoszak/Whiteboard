package org.samis.whiteboard.domain.repository

import kotlinx.coroutines.flow.Flow
import org.samis.whiteboard.domain.model.Whiteboard

interface WhiteboardRepository {

    fun getAllWhiteboards(): Flow<List<Whiteboard>>

    suspend fun deleteWhiteboard(whiteboard: Whiteboard)

    suspend fun upsertWhiteboard(whiteboard: Whiteboard): Long

    suspend fun getWhiteboardById(id: Long): Whiteboard?
}