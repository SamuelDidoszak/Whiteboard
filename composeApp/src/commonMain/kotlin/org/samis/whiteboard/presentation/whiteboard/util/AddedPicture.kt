package org.samis.whiteboard.presentation.whiteboard.util

import androidx.compose.ui.geometry.Offset

data class AddedPicture(
    var id: Long? = null,
    val picturePath: String,
    var position: Offset = Offset.Zero,
    var width: Int = 0,
    var height: Int = 0,
    var rotation: Float = 0f
)