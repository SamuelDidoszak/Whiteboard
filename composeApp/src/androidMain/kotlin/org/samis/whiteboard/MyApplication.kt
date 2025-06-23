package org.samis.whiteboard

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.samis.whiteboard.di.initKoin
import org.samis.whiteboard.presentation.util.AppScope

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@MyApplication)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        AppScope.cancel()
    }
}