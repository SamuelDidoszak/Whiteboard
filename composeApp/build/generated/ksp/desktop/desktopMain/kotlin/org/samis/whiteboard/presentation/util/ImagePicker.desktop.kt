package org.samis.whiteboard.presentation.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberImagePicker(onImagePicked: (uri: String) -> Unit): () -> Unit {
    TODO("Not yet implemented")
}

actual fun copyPictureToInternalStorage(path: String, whiteboardName: String, contextProvider: IContextProvider): String {
    TODO("Not yet implemented")
}