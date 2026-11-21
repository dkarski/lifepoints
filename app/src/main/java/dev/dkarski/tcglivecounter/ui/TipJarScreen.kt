package dev.dkarski.tcglivecounter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.text.TextMMD
import dev.dkarski.tcglivecounter.R
import dev.dkarski.tcglivecounter.ui.theme.AppTheme
import kotlin.math.floor

@Composable
fun TipJarScreen(url: String, contentPadding: PaddingValues = PaddingValues()) {
    Column(Modifier.fillMaxSize().padding(contentPadding).padding(16.dp)) {
        TextMMD(stringResource(R.string.tip_jar_text), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            QrCode(TipJarQr.ROWS, Modifier.size(200.dp).semantics { contentDescription = url })
            Spacer(Modifier.height(12.dp))
            TextMMD(
                text = url.removePrefix("https://"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Draws QR modules ("1" = dark) black on white with a 4-module quiet zone.
 * Module size and origin are whole pixels: fractional positions blur module edges into thin white seams.
 */
@Composable
private fun QrCode(rows: List<String>, modifier: Modifier) {
    Canvas(modifier) {
        val count = rows.size + 8
        val module = floor(size.minDimension / count)
        val side = module * count
        val origin = Offset(floor((size.width - side) / 2), floor((size.height - side) / 2))
        drawRect(Color.White, origin, Size(side, side))
        rows.forEachIndexed { y, row ->
            row.forEachIndexed { x, cell ->
                if (cell == '1') {
                    drawRect(Color.Black, Offset(origin.x + (x + 4) * module, origin.y + (y + 4) * module), Size(module, module))
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 520, showBackground = true)
@Composable
private fun TipJarPreview() {
    AppTheme {
        TipJarScreen("https://ko-fi.com/example")
    }
}
