package org.samis.whiteboard.presentation.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DelayedTask(private val scope: CoroutineScope, private val method: () -> Unit) {
    private var job: Job? = null

    fun start(delay: Long) {
        stop()
        job = scope.launch {
            delay(delay)
            method()
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}