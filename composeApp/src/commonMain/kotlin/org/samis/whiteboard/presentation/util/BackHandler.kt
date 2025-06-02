package org.samis.whiteboard.presentation.util

import androidx.compose.runtime.Composable

@Composable
expect fun registerBackHandler(onBack: () -> Unit)