package org.samis.whiteboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.LocalDate
import org.samis.whiteboard.domain.model.Whiteboard
import org.samis.whiteboard.presentation.dashboard.component.WhiteboardItemCard
import org.samis.whiteboard.presentation.whiteboard.component.ColorSelectionDialog

//@Preview
@Composable
fun PreviewWhiteboardItemCard() {
    WhiteboardItemCard(
        whiteboard = Whiteboard(
            name = "Drawing New",
            lastEdited = LocalDate(2025, 2, 19),
            canvasColor = Color.White
        ),
        onRenameClick = {},
        onDeleteClick = {}
    )
}

@Preview
@Composable
fun PreviewColorSelectionDialog() {
    ColorSelectionDialog(
        isOpen = true,
        onColorSelected = {},
        onDismiss = {}
    )
}