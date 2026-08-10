package org.samis.whiteboard.presentation.util

import org.samis.whiteboard.domain.model.DrawingTool

class DrawingToolVisibility(
    private val initialVisibility: Map<DrawingTool, Boolean> = emptyMap()
) {

    private val toolVisibility: Map<DrawingTool, Boolean> = DrawingTool.entries.associateWith { tool -> initialVisibility[tool] ?: true }.toMap()

    fun isToolVisible(tool: DrawingTool): Boolean {
        return toolVisibility[tool] ?: true
    }

    fun getAllToolStates(): Map<DrawingTool, Boolean> {
        return toolVisibility.toMap()
    }

    fun copy(drawingTool: DrawingTool, value: Boolean): DrawingToolVisibility {
        val toolMap = toolVisibility.toMutableMap()
        toolMap[drawingTool] = value
        return DrawingToolVisibility(toolMap)
    }
}
