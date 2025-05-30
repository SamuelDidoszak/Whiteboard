package org.samis.whiteboard.presentation

import android.app.Application
import android.content.Context
import android.os.Environment
import org.samis.whiteboard.presentation.util.IContextProvider
import java.io.File

class ContextProvider(application: Application) : IContextProvider {
    override val applicationContext: Context = application.applicationContext // Use concrete type

    override fun getExternalFilesDir(directoryType: String): File? {
        val androidDirType = when (directoryType) { // Map common string to Android dir
            "DIRECTORY_PICTURES" -> Environment.DIRECTORY_PICTURES
            "DIRECTORY_DOWNLOADS" -> Environment.DIRECTORY_DOWNLOADS
            else -> Environment.DIRECTORY_PICTURES // Default
        }
        return applicationContext.getExternalFilesDir(androidDirType)
    }
}