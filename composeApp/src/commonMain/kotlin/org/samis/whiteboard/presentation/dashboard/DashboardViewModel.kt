package org.samis.whiteboard.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.samis.whiteboard.domain.model.Update
import org.samis.whiteboard.domain.model.Whiteboard
import org.samis.whiteboard.domain.repository.PathRepository
import org.samis.whiteboard.domain.repository.SettingsRepository
import org.samis.whiteboard.domain.repository.UpdateRepository
import org.samis.whiteboard.domain.repository.WhiteboardRepository
import org.samis.whiteboard.presentation.util.AppScope
import org.samis.whiteboard.presentation.util.IContextProvider
import java.io.File

class DashboardViewModel(
    private val whiteboardRepository: WhiteboardRepository,
    private val updateRepository: UpdateRepository,
    private val pathRepository: PathRepository,
    private val settingsRepository: SettingsRepository,
    private val contextProvider: IContextProvider
    ): ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state = combine(
        _state,
        whiteboardRepository.getAllWhiteboards(),
        settingsRepository.getDashboardSize()
    ) { state, whiteboards, dashboardSize ->
        state.copy(
            whiteboards = whiteboards.sortedByDescending { it.createTime },
            dashboardSize = dashboardSize
        )
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

            is DashboardEvent.CopyWhiteboard -> {
                if (event.whiteboard == null) return
                cloneWhiteboard(event.whiteboard)
            }

            is DashboardEvent.ShowDeletePrompt -> {
                _state.update { it.copy(
                    whiteboardForUpdate = event.whiteboard,
                    isDeletePromptOpen = event.show
                ) }
            }

            is DashboardEvent.ShowRenamePrompt -> {
                _state.update { it.copy(
                    whiteboardForUpdate = event.whiteboard,
                    isRenamePromptOpen = event.show
                ) }
            }
        }
    }

    private fun updateWhiteboard(whiteboard: Whiteboard) {
        viewModelScope.launch {
            whiteboardRepository.upsertWhiteboard(whiteboard)
        }
    }

    private fun deleteWhiteboard(whiteboard: Whiteboard) {
        AppScope.scope.launch {
            val updates = updateRepository.getWhiteboardUpdates(whiteboard.id!!).first()
            updates.forEach {
                when (it) {
                    is Update.AddPath -> pathRepository.deletePath(it.path)
                    is Update.RemovePath -> pathRepository.deletePath(it.path)
                    is Update.Erase -> pathRepository.deletePath(it.path)
                    is Update.RemoveErase -> pathRepository.deletePath(it.path)
                }
                updateRepository.deleteUpdate(it)
            }
            whiteboardRepository.deleteWhiteboard(whiteboard)

            try {
                File(whiteboard.miniatureSrc!!).delete()
            } catch (e: Exception) { e.printStackTrace() }

        }
    }

    private fun cloneWhiteboard(whiteboard: Whiteboard) {
        val whiteboardName: String
        if (whiteboard.name.matches(Regex(".*\\s\\d+$"))) {
            val lastNum = whiteboard.name.substringAfterLast(' ').toInt()
            whiteboardName = whiteboard.name.replaceAfterLast(' ', (lastNum + 1).toString())
        } else
            whiteboardName = "${whiteboard.name} 2"

        val miniaturePath = whiteboardName.replace('/', '-')
        val newWhiteboard = Whiteboard(
            name = whiteboardName,
            createTime = Clock.System.now(),
            lastModified = Clock.System.now(),
            id = null,
            palette = whiteboard.palette,
            strokeWidths = whiteboard.strokeWidths,
            activeStrokeWidthButton = whiteboard.activeStrokeWidthButton,
            opacity = whiteboard.opacity,
            fillColor = whiteboard.fillColor,
            pointer = whiteboard.pointer,
            miniatureSrc = "$miniaturePath.png"
        )

        AppScope.scope.launch {
            var filePath = ""
            try {
                val directory = File(contextProvider.getExternalFilesDir("DIRECTORY_PICTURES"), "Whiteboard")
                if (!directory.exists())
                    directory.mkdirs()
                val file = File(whiteboard.miniatureSrc!!)
                var newFile = File(directory, "$miniaturePath.png")
                var num = 1
                if (newFile.exists()) {
                    do {
                        num++
                        newFile = File("${miniaturePath}_$num.png")
                    } while (file.exists())
                }
                file.copyTo(newFile, overwrite = true)
                filePath = newFile.path
            } catch (e: Error) { e.printStackTrace() }

            val whiteboardId = whiteboardRepository.upsertWhiteboard(newWhiteboard.copy(miniatureSrc = filePath))
            val updates = updateRepository.getWhiteboardUpdates(whiteboard.id!!).first()

            updates.forEach {
                it.id = null
                when (it) {
                    is Update.AddPath -> {
                        it.path.id = null
                        it.path.id = pathRepository.upsertPath(it.path)
                    }

                    is Update.RemovePath -> {
                        it.path.id = null
                        it.path.id = pathRepository.upsertPath(it.path)
                    }

                    is Update.Erase -> {
                        it.path.id = null
                        it.path.id = pathRepository.upsertPath(it.path)
                    }

                    is Update.RemoveErase -> {
                        it.path.id = null
                        it.path.id = pathRepository.upsertPath(it.path)
                    }
                }
                it.whiteboardId = whiteboardId
                upsertUpdate(it)
            }
        }
    }

    private fun upsertUpdate(update: Update) {
        viewModelScope.launch {
            updateRepository.upsertUpdate(update)
        }
    }
}