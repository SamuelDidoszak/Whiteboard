package org.samis.whiteboard.di

import org.koin.dsl.module
import org.samis.whiteboard.data.datastore.dataStore
import org.samis.whiteboard.data.database.getRoomDatabaseBuilder

actual val platformModule = module {
    single { getRoomDatabaseBuilder(get()) }
    single { dataStore(get()) }
}