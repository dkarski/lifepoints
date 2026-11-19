package dev.dkarski.tcglivecounter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp

@Composable
fun DottedDivider(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.outline
    Canvas(modifier.fillMaxWidth().height(1.dp)) {
        val stroke = 1.dp.toPx().coerceAtMost(1.5f).coerceAtLeast(1f) // one device pixel on the Kompakt
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = stroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f * stroke, 2.5f * stroke)),
        )
    }
}
