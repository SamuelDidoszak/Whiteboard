package org.samis.whiteboard.domain.model

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Instant

data class Whiteboard(
    val id: Long? = null,
    val name: String,
    val createTime: Instant,
    val lastModified: Instant,
    val canvasColor: Color,
    val pointer: Int? = null,
    val miniatureSrc: String? = null
)
