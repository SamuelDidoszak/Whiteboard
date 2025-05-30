package org.samis.whiteboard.di

import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.bind
import org.koin.dsl.module
import org.samis.whiteboard.data.database.getRoomDatabaseBuilder
import org.samis.whiteboard.data.datastore.dataStore
import org.samis.whiteboard.presentation.ContextProvider
import org.samis.whiteboard.presentation.util.IContextProvider

actual val platformModule = module {
    single { getRoomDatabaseBuilder(get()) }
    single { ContextProvider(androidApplication()) }.bind<IContextProvider>()
    single { dataStore(get()) }
}