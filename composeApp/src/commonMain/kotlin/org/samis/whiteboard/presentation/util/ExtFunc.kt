package org.samis.whiteboard.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

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

fun <T> List<T>.minusLast(element: T): List<T> {
    for (i in this.indices.reversed()) {
        if (this[i] == element)
            return this.subList(0, i) + this.subList(i + 1, this.size)
    }
    return this
}

fun Offset.rotateBy(degrees: Float): Offset {
    val radians = degrees * PI.toFloat() / 180f
    val cosine = cos(radians)
    val sine = sin(radians)
    return Offset(
        x = x * cosine - y * sine,
        y = x * sine + y * cosine
    )
}