package org.samis.whiteboard.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.samis.whiteboard.presentation.dashboard.component.DeleteWhiteboardDialog
import org.samis.whiteboard.presentation.dashboard.component.RenameWhiteboardDialog
import org.samis.whiteboard.presentation.dashboard.component.WhiteboardItemCard

@Composable
fun DashboardScreen(
    state: DashboardState,
    onEvent: (DashboardEvent) -> Unit,
    onSettingsIconClick: () -> Unit,
    onAddNewWhiteboardClick: () -> Unit,
    onCardClick: (Long) -> Unit
) {

    RenameWhiteboardDialog(
        state.whiteboardForUpdate?.name ?: "",
        "Rename",
        state.isRenamePromptOpen,
        { onEvent(DashboardEvent.ShowRenamePrompt(state.whiteboardForUpdate, false)) },
        { text ->
            onEvent(DashboardEvent.RenameWhiteboard(state.whiteboardForUpdate, text))
            onEvent(DashboardEvent.ShowRenamePrompt(state.whiteboardForUpdate, false))
        }
    )

    DeleteWhiteboardDialog(
        state.whiteboardForUpdate?.name ?: "",
        state.isDeletePromptOpen,
        { onEvent(DashboardEvent.ShowDeletePrompt(state.whiteboardForUpdate, false)) },
        {
            onEvent(DashboardEvent.DeleteWhiteboard(state.whiteboardForUpdate))
            onEvent(DashboardEvent.ShowDeletePrompt(state.whiteboardForUpdate, false))
        }
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DashboardTopBar(onSettingsIconClick = onSettingsIconClick)
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.whiteboards) { whiteboard ->
                    WhiteboardItemCard(
                        modifier = Modifier.clickable {
                            whiteboard.id?.let { onCardClick(it) }
                        },
                        whiteboard = whiteboard,
                        onRenameClick = { onEvent(DashboardEvent.ShowRenamePrompt(whiteboard, true)) },
                        onCopyClick = { onEvent(DashboardEvent.CopyWhiteboard(whiteboard)) },
                        onDeleteClick = { onEvent(DashboardEvent.ShowDeletePrompt(whiteboard, true)) }
                    )
                }
            }
        }
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onClick = onAddNewWhiteboardClick
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "New Whiteboard"
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    modifier: Modifier = Modifier,
    onSettingsIconClick: () -> Unit
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = "Dashboard") },
        actions = {
            IconButton(onClick = onSettingsIconClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}