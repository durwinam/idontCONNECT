package ir.idontconnect.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ir.idontconnect.core.model.TrafficSample

@Composable
fun TrafficChart(
    samples: List<TrafficSample>,
    modifier: Modifier = Modifier,
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val downloadColor = MaterialTheme.colorScheme.primary
    val uploadColor = MaterialTheme.colorScheme.secondary

    val animatedDownloads = samples.mapIndexed { index, sample ->
        val value by animateFloatAsState(
            targetValue = sample.downloadBytesPerSecond.coerceAtLeast(0L).toFloat(),
            animationSpec = tween(durationMillis = 750),
            label = "download-point-$index",
        )
        value
    }
    val animatedUploads = samples.mapIndexed { index, sample ->
        val value by animateFloatAsState(
            targetValue = sample.uploadBytesPerSecond.coerceAtLeast(0L).toFloat(),
            animationSpec = tween(durationMillis = 750),
            label = "upload-point-$index",
        )
        value
    }
    val targetMaximum = samples.maxOfOrNull { sample ->
        maxOf(sample.downloadBytesPerSecond, sample.uploadBytesPerSecond).coerceAtLeast(0L)
    }?.toFloat()?.coerceAtLeast(1f) ?: 1f
    val animatedMaximum by animateFloatAsState(
        targetValue = targetMaximum,
        animationSpec = tween(durationMillis = 750),
        label = "traffic-chart-scale",
    )

    Canvas(modifier.fillMaxWidth().height(150.dp)) {
        repeat(GRID_ROWS) { row ->
            val y = size.height * row / (GRID_ROWS - 1)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
            )
        }
        repeat(GRID_COLUMNS) { column ->
            val x = size.width * column / (GRID_COLUMNS - 1)
            drawLine(
                color = gridColor.copy(alpha = 0.55f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 0.7.dp.toPx(),
            )
        }

        if (samples.size < 2) return@Canvas

        fun drawSeries(values: List<Float>, color: Color) {
            val points = values.mapIndexed { index, value ->
                val x = size.width * index / (values.lastIndex).coerceAtLeast(1)
                val normalized = (value / animatedMaximum).coerceIn(0f, 1f)
                Offset(x, size.height * (1f - normalized))
            }
            val path = smoothPath(points)
            drawPath(
                path = path,
                color = color.copy(alpha = 0.12f),
                style = Stroke(width = 8.dp.toPx()),
            )
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.5.dp.toPx()),
            )
        }

        drawSeries(animatedDownloads, downloadColor)
        drawSeries(animatedUploads, uploadColor)
    }
}

private fun smoothPath(points: List<Offset>): Path = Path().apply {
    if (points.isEmpty()) return@apply
    moveTo(points.first().x, points.first().y)
    if (points.size == 1) return@apply

    for (index in 0 until points.lastIndex) {
        val current = points[index]
        val next = points[index + 1]
        val midpoint = Offset(
            x = (current.x + next.x) / 2f,
            y = (current.y + next.y) / 2f,
        )
        quadraticTo(current.x, current.y, midpoint.x, midpoint.y)
    }
    lineTo(points.last().x, points.last().y)
}

private const val GRID_ROWS = 5
private const val GRID_COLUMNS = 7
