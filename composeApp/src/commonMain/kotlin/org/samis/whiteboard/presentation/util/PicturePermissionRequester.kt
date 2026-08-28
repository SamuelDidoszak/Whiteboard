package org.samis.whiteboard.presentation.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPicturePermissionRequester(askedForPermissions: Boolean): (
    request: Boolean,
    onGranted: () -> Unit,
    onDenied: () -> Unit) -> Unit
