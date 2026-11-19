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
 * Single source of the app's colours. Components read colours only from [MaterialTheme.colorScheme]
 * (plus the helpers below), never from hard-coded values.
 */
enum class ThemeMode { Light, Dark }

/** MMD's E Ink scheme: black on white. */
val LightColors: ColorScheme = eInkColorScheme

/** MMD has no dark scheme: the light one with black and white swapped (still pure 1-bit for E Ink). */
val DarkColors: ColorScheme = eInkColorScheme.run {
    copy(
        primary = primary.inverted(), onPrimary = onPrimary.inverted(),
        primaryContainer = primaryContainer.inverted(), onPrimaryContainer = onPrimaryContainer.inverted(),
        inversePrimary = inversePrimary.inverted(),
        secondary = secondary.inverted(), onSecondary = onSecondary.inverted(),
        secondaryContainer = secondaryContainer.inverted(), onSecondaryContainer = onSecondaryContainer.inverted(),
        tertiary = tertiary.inverted(), onTertiary = onTertiary.inverted(),
        tertiaryContainer = tertiaryContainer.inverted(), onTertiaryContainer = onTertiaryContainer.inverted(),
        background = background.inverted(), onBackground = onBackground.inverted(),
        surface = surface.inverted(), onSurface = onSurface.inverted(),
        surfaceVariant = surfaceVariant.inverted(), onSurfaceVariant = onSurfaceVariant.inverted(),
        surfaceTint = surfaceTint.inverted(),
        inverseSurface = inverseSurface.inverted(), inverseOnSurface = inverseOnSurface.inverted(),
        error = error.inverted(), onError = onError.inverted(),
        errorContainer = errorContainer.inverted(), onErrorContainer = onErrorContainer.inverted(),
        outline = outline.inverted(), outlineVariant = outlineVariant.inverted(),
        scrim = scrim.inverted(),
        surfaceContainerLow = surfaceContainerLow.inverted(),
    )
}

private fun Color.inverted(): Color = when (this) {
    black -> white
    white -> black
    else -> this
}

fun ThemeMode.colors(): ColorScheme = if (this == ThemeMode.Dark) DarkColors else LightColors

/** Alpha for "not available yet" content. The only place where grey appears (E Ink rule). */
private const val MUTED_ALPHA = 0.6f

/** Content colour for disabled / coming-soon elements. */
val ColorScheme.muted: Color get() = onSurface.copy(alpha = MUTED_ALPHA)

/** Border width shared by tiles, outlined buttons and counter controls. */
val BorderWidth = 2.dp

/** Theme-aware outline (MMD's OutlinedButton border is hard-coded black). */
@Composable
@ReadOnlyComposable
fun outlineBorder(muted: Boolean = false): BorderStroke =
    BorderStroke(BorderWidth, if (muted) MaterialTheme.colorScheme.muted else MaterialTheme.colorScheme.outline)

@Composable
fun AppTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    ThemeMMD(colorScheme = mode.colors(), content = content)
}
