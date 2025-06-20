package org.samis.whiteboard.presentation.dashboard

import org.samis.whiteboard.domain.model.Whiteboard

data class DashboardState(
    val whiteboards: List<Whiteboard> = emptyList(),
    val whiteboardForUpdate: Whiteboard? = null,
    val isDeletePromptOpen: Boolean = false,
    val isRenamePromptOpen: Boolean = false
)
