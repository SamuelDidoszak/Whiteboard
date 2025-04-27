package org.samis.whiteboard.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.samis.whiteboard.data.database.dao.UpdateDao
import org.samis.whiteboard.data.mapper.toUpdate
import org.samis.whiteboard.data.mapper.toUpdateEntity
import org.samis.whiteboard.domain.model.UpdateType
import org.samis.whiteboard.domain.repository.PathRepository
import org.samis.whiteboard.domain.repository.UpdateRepository

class UpdateRepositoryImpl(
    private val updateDao: UpdateDao,
    private val pathRepository: PathRepository
): UpdateRepository {

    override suspend fun upsertUpdate(update: UpdateType) {
        updateDao.upsertUpdate(update.toUpdateEntity())
    }

    override suspend fun deleteUpdate(update: UpdateType) {
        updateDao.deleteUpdate(update.toUpdateEntity())
    }

    override fun getWhiteboardUpdates(whiteboardId: Long): Flow<List<UpdateType>> {
        return updateDao.getWhiteboardUpdates(whiteboardId).flatMapLatest { updateEntities ->
            flow {
                val updateTypes = updateEntities.map { updateEntity ->
                    val pathEntity = updateEntity.pathId?.let { pathRepository.getPathById(it) }
                    pathEntity?.let {
                        updateEntity.toUpdate(pathEntity)
                    }
                }.filterNotNull()
                emit(updateTypes)
            }
        }
    }
}