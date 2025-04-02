package org.samis.whiteboard.presentation.whiteboard.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import whiteboard.composeapp.generated.resources.Res
import whiteboard.composeapp.generated.resources.ic_redo
import whiteboard.composeapp.generated.resources.ic_undo

@Composable
fun TopBarHorizontal(
    modifier: Modifier = Modifier,
    onHomeIconClick: () -> Unit,
    onMenuIconClick: () -> Unit,
    onSaveIconClick: () -> Unit,
    onUndoIconClick: () -> Unit,
    onRedoIconClick: () -> Unit
) {
    Row(modifier = modifier) {
        FilledIconButton(onClick = { onHomeIconClick() }) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Home",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        FilledIconButton(onClick = { onUndoIconClick() }) {
            Icon(
                painter = painterResource(Res.drawable.ic_undo),
                contentDescription = "Undo",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
        FilledIconButton(onClick = { onRedoIconClick() }) {
            Icon(
                painter = painterResource(Res.drawable.ic_redo),
                contentDescription = "Redo",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
        FilledIconButton(onClick = { onMenuIconClick() }) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Command Palette",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
        FilledIconButton(onClick = { onSaveIconClick() }) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = "Save As Picture",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@Composable
fun TopBarVertical(
    modifier: Modifier = Modifier,
    onHomeIconClick: () -> Unit,
    onMenuIconClick: () -> Unit,
    onSaveIconClick: () -> Unit,
    onUndoIconClick: () -> Unit,
    onRedoIconClick: () -> Unit
) {
    Column(modifier = modifier) {
        FilledIconButton(onClick = { onHomeIconClick() }) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = "Home",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
        FilledIconButton(onClick = { onMenuIconClick() }) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "Command Palette",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        FilledIconButton(onClick = { onUndoIconClick() }) {
            Icon(
                painter = painterResource(Res.drawable.ic_undo),
                contentDescription = "Undo",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
        FilledIconButton(onClick = { onRedoIconClick() }) {
            Icon(
                painter = painterResource(Res.drawable.ic_redo),
                contentDescription = "Redo",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        FilledIconButton(onClick = { onSaveIconClick() }) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = "Save As Picture",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}