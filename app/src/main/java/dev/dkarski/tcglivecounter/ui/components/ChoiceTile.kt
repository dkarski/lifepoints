package dev.dkarski.tcglivecounter.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.text.TextMMD
import dev.dkarski.tcglivecounter.ui.theme.muted
import dev.dkarski.tcglivecounter.ui.theme.outlineBorder

val TileHeight = 56.dp

/** Taller tile used where the value carries its own caption (Home), for a bigger touch target. */
val CaptionTileHeight = 72.dp

/**
 * A selectable tile: filled when selected, outlined otherwise.
 * [caption] is a small line under the label (e.g. the unit "life"), so no separate section heading is needed.
 * [unavailable] marks an option that is not built yet (muted outline and text), still tappable.
 */
@Composable
fun ChoiceTile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
    unavailable: Boolean = false,
    height: Dp = if (caption != null) CaptionTileHeight else TileHeight,
) {
    val tileModifier = modifier
        .height(height)
        .semantics {
            this.selected = selected
            role = Role.RadioButton
            contentDescription = listOfNotNull(label, caption).joinToString(" ")
        }
    val textColor = if (unavailable) MaterialTheme.colorScheme.muted else Color.Unspecified
    val content: @Composable () -> Unit = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextMMD(
                text = label,
                style = if (caption != null) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
            )
            if (caption != null) {
                TextMMD(text = caption, style = MaterialTheme.typography.labelMedium, color = textColor, maxLines = 1)
            }
        }
    }
    if (selected) {
        ButtonMMD(onClick = onClick, modifier = tileModifier) { content() }
    } else {
        OutlinedButtonMMD(
            onClick = onClick,
            modifier = tileModifier,
            border = outlineBorder(muted = unavailable),
        ) { content() }
    }
}
