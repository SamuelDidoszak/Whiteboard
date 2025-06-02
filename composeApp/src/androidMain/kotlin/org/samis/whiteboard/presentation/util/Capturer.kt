package org.samis.whiteboard.presentation.util

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.datastore.core.IOException
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.CaptureController
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

actual fun capture(
    scope: CoroutineScope,
    controller: Any,
    contextProvider: IContextProvider,
    fileName: String,
    miniature: Boolean,
    miniaturePath: String?,
    onFileSave: (file: File) -> Unit
) {
    val captureController = controller as? CaptureController ?: return
    scope.launch {
        val bitmapAsync = captureController.captureAsync()
        withContext(Dispatchers.IO) {
            try {
                val bitmap = bitmapAsync.await()
                val byteArray = bitmap.asAndroidBitmap().toByteArray()

                val directory = File(contextProvider.getExternalFilesDir("DIRECTORY_PICTURES"), "Whiteboard")
                if (!directory.exists()) {
                    directory.mkdirs()
                }

                val fileName = fileName.replace('/', '-')
                var file = File(directory, "$fileName.png")
                var num = 0

                if (file.exists()) {
                    val currentMiniatureFilename = miniaturePath?.substringAfterLast('/')?.substringBefore('.')

                    if (currentMiniatureFilename != null && miniature)
                        file = File(directory, currentMiniatureFilename)
                    else {
                        do {
                            num++
                            file = File(directory, "${fileName}_$num.png")
                        } while (file.exists())
                    }
                }

                try {
                    val outputStream = BufferedOutputStream(FileOutputStream(file))
                    outputStream.write(byteArray)
                    outputStream.flush()
                    outputStream.close()
                    onFileSave(file)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (error: Throwable) {
                error.printStackTrace()
            }
        }
    }
}

private fun Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.capturable(controller: Any?): Modifier {
    val controller = controller as? CaptureController
    return if (controller != null) this.capturable(controller) else this
}

@Composable
actual fun rememberCaptureController(): Any {
    return rememberCaptureController()
}