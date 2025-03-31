package org.samis.whiteboard.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.samis.whiteboard.data.database.AppDatabase
import org.samis.whiteboard.data.database.getRoomDatabase
import org.samis.whiteboard.presentation.whiteboard.WhiteboardViewModel
import org.samis.whiteboard.presentation.settings.SettingsViewModel
import org.samis.whiteboard.data.repository.PathRepositoryImpl
import org.samis.whiteboard.data.repository.SettingRepositoryImpl
import org.samis.whiteboard.data.repository.WhiteboardRepositoryImpl
import org.samis.whiteboard.domain.repository.PathRepository
import org.samis.whiteboard.domain.repository.SettingsRepository
import org.samis.whiteboard.domain.repository.WhiteboardRepository
import org.samis.whiteboard.presentation.dashboard.DashboardViewModel

val sharedModule = module {

    //Database
    single { getRoomDatabase(get()) }
    single { get<AppDatabase>().pathDao() }
    single { get<AppDatabase>().whiteboardDao() }

    //ViewModels
    viewModelOf(::WhiteboardViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::DashboardViewModel)

    //Repositories
    singleOf(::PathRepositoryImpl).bind<PathRepository>()
    singleOf(::SettingRepositoryImpl).bind<SettingsRepository>()
    singleOf(::WhiteboardRepositoryImpl).bind<WhiteboardRepository>()
}