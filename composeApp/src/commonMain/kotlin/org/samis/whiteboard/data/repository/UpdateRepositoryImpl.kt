package org.samis.whiteboard.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import org.samis.whiteboard.data.database.dao.UpdateDao
import org.samis.whiteboard.data.mapper.toUpdate
import org.samis.whiteboard.data.mapper.toUpdateEntity
import org.samis.whiteboard.domain.model.Update
import org.samis.whiteboard.domain.repository.PathRepository
import org.samis.whiteboard.domain.repository.UpdateRepository

class UpdateRepositoryImpl(
    private val updateDao: UpdateDao,
    private val pathRepository: PathRepository
): UpdateRepository {

    override suspend fun upsertUpdate(update: Update): Long {
        return updateDao.upsertUpdate(update.toUpdateEntity())
    }

    override suspend fun deleteUpdate(update: Update) {
        updateDao.deleteUpdate(update.toUpdateEntity())
    }

    override fun getWhiteboardUpdates(whiteboardId: Long): Flow<List<Update>> {
        return updateDao.getWhiteboardUpdates(whiteboardId).flatMapLatest { updateEntities ->
            flow {
                val updateTypes = updateEntities.map { updateEntity ->
                    val pathEntity = updateEntity.pathId?.let { pathRepository.getPathById(it) }
                    if (pathEntity == null)
                        System.err.println("No path found for $updateEntity")
                    pathEntity?.let {
                        updateEntity.toUpdate(pathEntity)
                    }
                }.filterNotNull()
                emit(updateTypes)
            }
        }
    }
}