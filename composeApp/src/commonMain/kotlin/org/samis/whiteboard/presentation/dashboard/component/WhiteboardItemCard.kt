package org.samis.whiteboard.presentation.dashboard.component

import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.jetbrains.compose.resources.painterResource
import org.samis.whiteboard.domain.model.Whiteboard
import org.samis.whiteboard.presentation.util.formatDate
import whiteboard.composeapp.generated.resources.Res
import whiteboard.composeapp.generated.resources.img_canvas
import java.io.File

@Composable
fun WhiteboardItemCard(
    modifier: Modifier = Modifier,
    whiteboard: Whiteboard,
    onRenameClick: () -> Unit,
    onCopyClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var miniatureFile = remember(whiteboard.miniatureSrc) {
        if (whiteboard.miniatureSrc != null)
            File(whiteboard.miniatureSrc)
        else null
    }
    val cacheKey = "${miniatureFile?.path}-${miniatureFile?.lastModified()}"

    if (miniatureFile == null) {
        LaunchedEffect(Unit) {
            miniatureFile =
                if (whiteboard.miniatureSrc != null)
                    File(whiteboard.miniatureSrc)
                else null
        }
    }

    var isExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(miniatureFile)
                .memoryCacheKey(cacheKey)
                .diskCacheKey(cacheKey)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(Res.drawable.img_canvas),
            fallback = painterResource(Res.drawable.img_canvas),
            modifier = Modifier.fillMaxWidth().height(156.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = SpaceBetween
        ) {
            Text(
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                text = whiteboard.name,
                maxLines = 2
            )
            Box(
                modifier = Modifier.weight(0.25f)
            ) {
                IconButton(
                    onClick = { isExpanded = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options"
                    )
                }
                WhiteboardCardMoreOptionsMenu(
                    isExpanded = isExpanded,
                    onMenuDismiss = { isExpanded = false },
                    onRenameClick = onRenameClick,
                    onCopyClick = onCopyClick,
                    onDeleteClick = onDeleteClick
                )
            }
        }
        Text(
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            text = "Last edited: ${whiteboard.lastEdited.formatDate()}",
            style = MaterialTheme.typography.labelSmall
        )
    }
}