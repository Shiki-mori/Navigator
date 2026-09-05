package dev.phrolova.navigator.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

data class ChartLine(
    val name: String,
    val color: Color,
    val values: List<Float>,
)

@Composable
fun MultiLineChart(
    xLabels: List<String>,
    lines: List<ChartLine>,
    modifier: Modifier = Modifier,
    showValues: Boolean = false,
) {
    val textMeasurer = rememberTextMeasurer()
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    val labelStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp,
    )
    val pointCount = xLabels.size
    val maxValue = remember(lines) {
        val raw = lines.maxOfOrNull { line -> line.values.maxOrNull() ?: 0f } ?: 0f
        if (raw <= 1f) 1f else ceil(raw)
    }
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(if (showValues) 260.dp else 220.dp),
    ) {
        if (pointCount == 0) return@Canvas
        val startPad = 36.dp.toPx()
        val endPad = 8.dp.toPx()
        val topPad = if (showValues) (16.dp.toPx() + lines.size * 14.dp.toPx()) else 12.dp.toPx()
        val bottomPad = 24.dp.toPx()
        val plotWidth = size.width - startPad - endPad
        val plotHeight = size.height - topPad - bottomPad
        val yTicks = if (maxValue <= 1f) listOf(0f, 1f) else listOf(0f, maxValue / 2f, maxValue)

        fun xOf(index: Int): Float {
            return if (pointCount == 1) {
                startPad + plotWidth / 2f
            } else {
                startPad + plotWidth * index / (pointCount - 1)
            }
        }

        fun yOf(value: Float): Float {
            val ratio = (value / maxValue).coerceIn(0f, 1f)
            return topPad + plotHeight * (1f - ratio)
        }

        yTicks.forEach { tick ->
            val y = yOf(tick)
            drawLine(
                color = axisColor.copy(alpha = 0.35f),
                start = Offset(startPad, y),
                end = Offset(size.width - endPad, y),
                strokeWidth = 1.dp.toPx(),
            )
            val label = if (tick % 1f == 0f) tick.toInt().toString() else tick.toString()
            val layout = textMeasurer.measure(label, labelStyle)
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    x = (startPad - layout.size.width - 6.dp.toPx()).coerceAtLeast(0f),
                    y = y - layout.size.height / 2f,
                ),
            )
        }

        visibleXIndices(pointCount).forEach { index ->
            val layout = textMeasurer.measure(xLabels[index], labelStyle)
            val x = xOf(index) - layout.size.width / 2f
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(
                    x = x.coerceIn(0f, size.width - layout.size.width),
                    y = size.height - layout.size.height,
                ),
            )
        }

        lines.forEach { line ->
            if (line.values.isEmpty()) return@forEach
            val path = Path()
            line.values.forEachIndexed { index, value ->
                val point = Offset(xOf(index), yOf(value))
                if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
            }
            if (line.values.size > 1) {
                drawPath(
                    path = path,
                    color = line.color,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
                )
            }
            line.values.forEachIndexed { index, value ->
                drawCircle(
                    color = line.color,
                    radius = 3.5.dp.toPx(),
                    center = Offset(xOf(index), yOf(value)),
                )
            }
        }

        if (showValues) {
            lines.forEachIndexed { seriesIndex, line ->
                val valueStyle = TextStyle(
                    color = line.color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
                line.values.forEachIndexed { index, value ->
                    val text = formatCount(value)
                    val layout = textMeasurer.measure(text, valueStyle)
                    val point = Offset(xOf(index), yOf(value))
                    val lift = 6.dp.toPx() + seriesIndex * (layout.size.height + 2.dp.toPx())
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(
                            x = (point.x - layout.size.width / 2f).coerceIn(
                                startPad,
                                size.width - layout.size.width,
                            ),
                            y = (point.y - layout.size.height - lift).coerceAtLeast(0f),
                        ),
                    )
                }
            }
        }
    }
}

internal fun formatCount(value: Float): String {
    return if (value % 1f == 0f) value.toInt().toString() else value.toString()
}

private fun visibleXIndices(count: Int): List<Int> {
    if (count <= 12) return (0 until count).toList()
    val step = if (count <= 20) 2 else 5
    val indices = (0 until count step step).toMutableList()
    if (indices.last() != count - 1) {
        indices.add(count - 1)
    }
    return indices
}
