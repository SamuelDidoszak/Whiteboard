package org.samis.whiteboard.presentation.util

import java.io.File

interface IContextProvider {
    val applicationContext: Any
    fun getExternalFilesDir(directoryType: String): File?
}