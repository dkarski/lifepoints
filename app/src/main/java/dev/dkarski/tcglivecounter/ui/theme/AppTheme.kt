package dev.dkarski.tcglivecounter.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mudita.mmd.ThemeMMD
import com.mudita.mmd.black
import com.mudita.mmd.eInkColorScheme
import com.mudita.mmd.white

/**
 * Single source of the app's colours. The app has one look: MMD's E Ink scheme, black on white. Components read
 * colours from [MaterialTheme.colorScheme] (plus the helpers below), and the counter from [PlayerColors];
 * never from hard-coded values. Only pure black and white are used (E Ink).
 */
val AppColors: ColorScheme = eInkColorScheme

/** The colours of one player's half of the table: [background], and [content] for the life total, the change
 * indicator, the −/+ symbols and outlines. A pressed button swaps them (filled with [content]). */
data class PlayerColors(val background: Color, val content: Color)

/** Top half of the screen (the player facing the top edge): white with black. */
val TopPlayerColors = PlayerColors(background = white, content = black)

/** Bottom half of the screen: black with white. */
val BottomPlayerColors = PlayerColors(background = black, content = white)

/**
 * Menu button on the border between the two halves: white with a black outline and icon. The white disc stands
 * out on the black half, the black outline on the white half.
 */
val MenuButtonColors = PlayerColors(background = white, content = black)

/** Full-screen frame shown for a moment before the counter, when returning from the menu: driving every pixel to
 * black clears the menu's ghost image from the E Ink panel. */
val ClearFrameColor: Color = black

/** Alpha for "not available yet" content. The only place where grey appears (E Ink rule). */
private const val MUTED_ALPHA = 0.6f

/** Content colour for disabled / coming-soon elements. */
val ColorScheme.muted: Color get() = onSurface.copy(alpha = MUTED_ALPHA)

/** Border width shared by tiles, outlined buttons and counter controls. */
val BorderWidth = 2.dp

/** Outline in the scheme's colour (MMD's OutlinedButton border is hard-coded). */
@Composable
@ReadOnlyComposable
fun outlineBorder(muted: Boolean = false): BorderStroke =
    BorderStroke(BorderWidth, if (muted) MaterialTheme.colorScheme.muted else MaterialTheme.colorScheme.outline)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    ThemeMMD(colorScheme = AppColors, content = content)
}
