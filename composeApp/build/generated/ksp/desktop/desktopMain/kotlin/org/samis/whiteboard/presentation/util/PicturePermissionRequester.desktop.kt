package org.samis.whiteboard.presentation.util

import androidx.compose.runtime.Composable

@Composable
actual fun rememberPicturePermissionRequester(askedForPermissions: Boolean): (
    request: Boolean,
    onGranted: () -> Unit,
    onDenied: () -> Unit) -> Unit = { _, onGranted, _ ->
        onGranted()
    }