package org.samis.whiteboard.presentation.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object AppScope {

    private val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.IO + job)

    fun cancel() {
        job.cancel()
    }
}