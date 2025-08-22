package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import whiteboard.composeapp.generated.resources.Res
import whiteboard.composeapp.generated.resources.ic_redo
import whiteboard.composeapp.generated.resources.ic_undo

@Composable
fun CommandBarHorizontal(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onHomeIconClick: () -> Unit,
    onMenuIconClick: () -> Unit,
    onSaveIconClick: () -> Unit,
    onUndoIconClick: () -> Unit,
    onRedoIconClick: () -> Unit
) {
    Row(modifier = modifier) {
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onHomeIconClick) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Home",
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onUndoIconClick) {
            Icon(
                painter = painterResource(Res.drawable.ic_undo),
                contentDescription = "Undo",
                modifier = Modifier.size(25.dp)
            )
        }
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onRedoIconClick) {
            Icon(
                painter = painterResource(Res.drawable.ic_redo),
                contentDescription = "Redo",
                modifier = Modifier.size(25.dp)
            )
        }
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onMenuIconClick) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Command Palette",
                modifier = Modifier.size(25.dp)
            )
        }
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onSaveIconClick) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = "Save As Picture",
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@Composable
fun CommandBarVertical(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    onHomeIconClick: () -> Unit,
    onMenuIconClick: () -> Unit,
    onSaveIconClick: () -> Unit,
    onUndoIconClick: () -> Unit,
    onRedoIconClick: () -> Unit
) {
    Column(modifier = modifier) {
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onHomeIconClick) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Home",
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onMenuIconClick) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Command Palette",
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onUndoIconClick) {
            Icon(
                painter = painterResource(Res.drawable.ic_undo),
                contentDescription = "Undo",
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onRedoIconClick) {
            Icon(
                painter = painterResource(Res.drawable.ic_redo),
                contentDescription = "Redo",
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        ElevatedIconButton(backgroundColor = backgroundColor, isSelected = false, onClick = onSaveIconClick) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = "Save As Picture",
                modifier = Modifier.size(25.dp)
            )
        }
    }
}
