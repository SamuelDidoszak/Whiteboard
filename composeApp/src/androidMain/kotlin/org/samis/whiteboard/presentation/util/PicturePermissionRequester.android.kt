package org.samis.whiteboard.presentation.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
actual fun rememberPicturePermissionRequester(askedForPermissions: Boolean): (
    request: Boolean,
    onGranted: () -> Unit,
    onDenied: () -> Unit) -> Unit {

    val context = LocalContext.current
    val permissions = if (Build.VERSION.SDK_INT >= 33)
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    else
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    var onGrantedReference by remember { mutableStateOf({}) }
    var onDeniedReference by remember { mutableStateOf({}) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) onGrantedReference() else onDeniedReference()
    }

    return { request, onGranted, onDenied ->
        onGrantedReference = onGranted
        onDeniedReference = onDenied

        val alreadyGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        val isPermanentlyDenied = permissions.any {
            !ActivityCompat.shouldShowRequestPermissionRationale(context.findActivity()!!, it) &&
                    ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (alreadyGranted)
            onGranted()
        else if (request) {
            if (askedForPermissions && isPermanentlyDenied) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
                onDenied()
            } else
                launcher.launch(permissions)
        } else
            onDenied()
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}