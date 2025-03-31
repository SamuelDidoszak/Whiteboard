package org.samis.whiteboard.di

import org.koin.dsl.module
import org.samis.whiteboard.data.datastore.createDataStore
import org.samis.whiteboard.data.database.getRoomDatabaseBuilder
import org.samis.whiteboard.data.util.Constant.DATA_STORE_FILE_NAME

actual val platformModule = module {
    single { getRoomDatabaseBuilder() }
    single { createDataStore { DATA_STORE_FILE_NAME } }
}