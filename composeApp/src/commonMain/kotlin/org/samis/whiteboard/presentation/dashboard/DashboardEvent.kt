package org.samis.whiteboard.presentation.dashboard

import org.samis.whiteboard.domain.model.Whiteboard

sealed class DashboardEvent {
    data class DeleteWhiteboard(val whiteboard: Whiteboard?): DashboardEvent()
    data class RenameWhiteboard(val whiteboard: Whiteboard?, val name: String): DashboardEvent()

    data class ShowDeletePrompt(val whiteboard: Whiteboard?, val show: Boolean): DashboardEvent()
    data class ShowRenamePrompt(val whiteboard: Whiteboard?, val show: Boolean): DashboardEvent()
}