package org.samis.whiteboard.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.samis.whiteboard.domain.model.Whiteboard
import org.samis.whiteboard.domain.repository.WhiteboardRepository

class DashboardViewModel(
    private val repository: WhiteboardRepository
): ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state = combine(
        _state,
        repository.getAllWhiteboards()
    ) { state, whiteboards ->
        state.copy(whiteboards = whiteboards.sortedByDescending { it.lastEdited })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = DashboardState()
    )

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.DeleteWhiteboard -> {
                if (event.whiteboard == null) return
                deleteWhiteboard(event.whiteboard)
                _state.update { it.copy(whiteboardForUpdate = null) }
            }

            is DashboardEvent.RenameWhiteboard -> {
                if (event.whiteboard == null) return
                val whiteboard = event.whiteboard.copy(name = event.name)
                updateWhiteboard(whiteboard)
                _state.update { it.copy(whiteboardForUpdate = null) }
            }

            is DashboardEvent.ShowDeletePrompt -> {
                _state.update { it.copy(
                    whiteboardForUpdate = event.whiteboard,
                    isDeletePromptOpen = event.show
                ) }
            }

            is DashboardEvent.ShowRenamePrompt -> {
                println("Whiteboard: ${event.whiteboard}")
                _state.update { it.copy(
                    whiteboardForUpdate = event.whiteboard,
                    isRenamePromptOpen = event.show
                ) }
            }
        }
    }

    private fun deleteWhiteboard(whiteboard: Whiteboard) {
        viewModelScope.launch {
            repository.deleteWhiteboard(whiteboard)
        }
    }

    private fun updateWhiteboard(whiteboard: Whiteboard) {
        viewModelScope.launch {
            repository.upsertWhiteboard(whiteboard)
        }
    }
}