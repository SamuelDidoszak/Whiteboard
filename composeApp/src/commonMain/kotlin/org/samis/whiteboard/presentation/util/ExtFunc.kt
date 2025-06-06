package org.samis.whiteboard.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlin.math.roundToInt

fun LocalDate.formatDate(): String {
    return this.format(
        LocalDate.Format {
            dayOfMonth()
            chars("/")
            monthNumber()
            chars("/")
            year()
        }
    )
}

@Composable
fun Dp.toPx() = with(LocalDensity.current) { this@toPx.toPx() }

@Composable
fun Int.toDp() = with(LocalDensity.current) { this@toDp.toDp() }

fun Float.equalsDelta(other: Float, delta: Float = 0.0001f): Boolean = kotlin.math.abs(this - other) < delta

fun Float.roundTo(step: Float): Float = (this / step).roundToInt() * step