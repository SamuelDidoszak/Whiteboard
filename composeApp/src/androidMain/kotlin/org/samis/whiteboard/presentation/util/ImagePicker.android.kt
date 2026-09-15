package org.samis.whiteboard.presentation.util

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import java.io.File

@Composable
actual fun rememberImagePicker(onImagePicked: (uri: String) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri ->
        uri?.toString()?.let { onImagePicked(it) }
    }
    return { launcher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) }
}

actual fun copyPictureToInternalStorage(path: String, whiteboardName: String, contextProvider: IContextProvider): String {
    val context = contextProvider.applicationContext as Context
    val directory = File(contextProvider.getExternalFilesDir("DIRECTORY_PICTURES"), "Pictures")
    if (!directory.exists()) {
        directory.mkdirs()
    }

    val pictureName = whiteboardName.replace('/', '-')
    val extension = context.contentResolver.getType(path.toUri())?.substringAfterLast('/') ?: "png"
    val fileName = pictureName + "_picture_0.$extension"
    var file = File(directory, fileName)

    while (file.exists()) {
        val num = (file.name.substringAfterLast('_').substringBefore('.').toIntOrNull() ?: 0) + 1
        val newFileName = "${pictureName}_picture_$num.$extension"
        file = File(directory, newFileName)
    }

    context.contentResolver.openInputStream(path.toUri())!!.use { inputStream ->
        file.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }
    }
    return file.path
}