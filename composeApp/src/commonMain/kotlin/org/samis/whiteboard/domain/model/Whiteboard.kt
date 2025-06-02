package org.samis.whiteboard.domain.model

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate

data class Whiteboard(
    val id: Long? = null,
    val name: String,
    val lastEdited: LocalDate,
    val canvasColor: Color,
    val pointer: Int? = null,
    val miniatureSrc: String? = null
)
