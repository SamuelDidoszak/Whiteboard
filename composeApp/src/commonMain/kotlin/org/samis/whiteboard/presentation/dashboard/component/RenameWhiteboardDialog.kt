package org.samis.whiteboard.presentation.dashboard.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun RenameWhiteboardDialog(
    whiteboardName: String,
    prompt: String = "Rename whiteboard",
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(TextFieldValue()) }
    val focusRequester = remember { FocusRequester() }

    if (showDialog) {
        LaunchedEffect(Unit) {
            delay(100)
            focusRequester.requestFocus()
            text = TextFieldValue(
                text = whiteboardName,
                selection = TextRange(whiteboardName.length)
            )
        }
    } else return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = prompt,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onSave(text.text) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 8.dp,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

