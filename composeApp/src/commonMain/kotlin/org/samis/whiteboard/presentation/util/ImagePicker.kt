package org.samis.whiteboard.presentation.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePicker(onImagePicked: (uri: String) -> Unit): () -> Unit

expect fun copyPictureToInternalStorage(path: String, whiteboardName: String, contextProvider: IContextProvider): String