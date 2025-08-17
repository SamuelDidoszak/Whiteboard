package org.samis.whiteboard.presentation.dashboard

import org.samis.whiteboard.domain.model.Whiteboard
import org.samis.whiteboard.presentation.settings.util.DashboardSizeOption

data class DashboardState(
    val whiteboards: List<Whiteboard> = emptyList(),
    val whiteboardForUpdate: Whiteboard? = null,
    val isDeletePromptOpen: Boolean = false,
    val isRenamePromptOpen: Boolean = false,
    val dashboardSize: DashboardSizeOption = DashboardSizeOption.MEDIUM
)
