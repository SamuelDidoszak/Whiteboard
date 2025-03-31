package org.samis.whiteboard.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.samis.whiteboard.data.database.dao.WhiteboardDao
import org.samis.whiteboard.data.mapper.toWhiteboard
import org.samis.whiteboard.data.mapper.toWhiteboardEntity
import org.samis.whiteboard.data.mapper.toWhiteboardList
import org.samis.whiteboard.domain.model.Whiteboard
import org.samis.whiteboard.domain.repository.WhiteboardRepository

class WhiteboardRepositoryImpl(
    private val whiteboardDao: WhiteboardDao
): WhiteboardRepository {

    override fun getAllWhiteboards(): Flow<List<Whiteboard>> {
        return whiteboardDao.getAllWhiteboards().map { it.toWhiteboardList() }
    }

    override suspend fun upsertWhiteboard(whiteboard: Whiteboard): Long {
        return if (whiteboard.id == null) {
            whiteboardDao.insertWhiteboard(whiteboard.toWhiteboardEntity())
        } else {
            whiteboardDao.updateWhiteboard(whiteboard.toWhiteboardEntity())
            whiteboard.id
        }
    }

    override suspend fun getWhiteboardById(id: Long): Whiteboard? {
        return whiteboardDao.getWhiteboardById(id)?.toWhiteboard()
    }
}