package org.samis.whiteboard.presentation.whiteboard.util

class UniqueIdGenerator {
    private var id = -1L

    fun getId(): Long {
        return ++id
    }
}