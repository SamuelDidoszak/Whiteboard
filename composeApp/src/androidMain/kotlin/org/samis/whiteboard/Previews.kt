package org.samis.whiteboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.Instant
import org.samis.whiteboard.domain.model.Whiteboard
import org.samis.whiteboard.presentation.dashboard.component.WhiteboardItemCard
import org.samis.whiteboard.presentation.whiteboard.component.ColorSelectionDialog

//@Preview
@Composable
fun PreviewWhiteboardItemCard() {
    WhiteboardItemCard(
        whiteboard = Whiteboard(
            name = "Drawing New",
            createTime = Instant.parse("2024-06-25T10:15:30Z"),
            lastModified = Instant.parse("2024-06-25T10:15:30Z"),
            canvasColor = Color.White
        ),
        onRenameClick = {},
        onCopyClick = {},
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