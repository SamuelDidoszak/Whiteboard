package org.samis.whiteboard.presentation.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DelayedTask<T>(private val scope: CoroutineScope, private val method: (T) -> Unit) {
    private var job: Job? = null

    fun start(delay: Long, data: T) {
        stop()
        job = scope.launch {
            delay(delay)
            method(data)
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}