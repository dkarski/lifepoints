package dev.dkarski.tcglivecounter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.buttons.ButtonDefaultsMMD
import com.mudita.mmd.components.text.TextMMD
import kotlinx.coroutines.delay
import dev.dkarski.tcglivecounter.R
import dev.dkarski.tcglivecounter.ui.theme.AppTheme
import dev.dkarski.tcglivecounter.ui.theme.BorderWidth
import dev.dkarski.tcglivecounter.ui.theme.ThemeMode

/**
 * Fullscreen counter for two players facing each other across the table.
 * Player 1 sits at the bottom edge; Player 2's half is rotated 180° so it reads from the top edge.
 */
@Composable
fun GameCounterScreen(
    lives: List<Int>,
    onLifeChange: (player: Int, delta: Int) -> Unit,
    onMenu: () -> Unit,
) {
    // Keep the screen awake only while the counter is visible (window flag, no permission or wake lock).
    // Leaving the counter, or the app, lets the system timeout work again.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(Modifier.fillMaxSize()) {
            PlayerPanel(
                player = 1,
                life = lives[1],
                onChange = { onLifeChange(1, it) },
                modifier = Modifier.weight(1f).rotate(180f),
            )
            PlayerPanel(
                player = 0,
                life = lives[0],
                onChange = { onLifeChange(0, it) },
                modifier = Modifier.weight(1f),
            )
        }
        MenuButton(onClick = onMenu, modifier = Modifier.align(Alignment.Center))
    }
}

/** One player's controls, top to bottom from their own perspective: + / life / −. */
@Composable
private fun PlayerPanel(
    player: Int,
    life: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val name = stringResource(R.string.player_name, player + 1)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StepButton(
            symbol = "+",
            description = stringResource(R.string.counter_increase, name),
            onClick = { onChange(+1) },
            modifier = Modifier.weight(1f),
        )
        // No player name: names cannot be edited yet and each half already faces its player.
        // The name stays in the buttons' content descriptions for screen readers.
        Box(
            modifier = Modifier
                .weight(1.9f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            TextMMD(
                text = life.toString(),
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.wrapContentHeight(unbounded = true),
            )
        }
        StepButton(
            symbol = "−",
            description = stringResource(R.string.counter_decrease, name),
            onClick = { onChange(-1) },
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Large +/− control. MMD has no pressed state (ButtonMMD ignores interactions), so the button inverts its
 * fill while pressed and for a short minimum time after a quick tap. It is a single static redraw of this
 * button only (no ripple, no animation), so the tap is visible even when the E Ink panel lags behind.
 */
@Composable
private fun StepButton(symbol: String, description: String, onClick: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    var highlighted by remember { mutableStateOf(false) }
    LaunchedEffect(pressed) {
        if (pressed) {
            highlighted = true
        } else if (highlighted) {
            delay(PRESS_FEEDBACK_MS)
            highlighted = false
        }
    }
    val shape = ButtonDefaultsMMD.shape
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (highlighted) colors.onSurface else colors.surface)
            .border(BorderWidth, colors.outline, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        TextMMD(
            text = symbol,
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = if (highlighted) colors.surface else colors.onSurface,
        )
    }
}

/** Minimum time the pressed state stays visible after a quick tap. */
private const val PRESS_FEEDBACK_MS = 150L

@Composable
private fun MenuButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderWidth, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(onClick = onClick, interactionSource = null, indication = null),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = stringResource(R.string.counter_open_menu),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(widthDp = 320, heightDp = 533, showBackground = true)
@Composable
private fun GameCounterPreview() {
    AppTheme(ThemeMode.Light) { GameCounterScreen(lives = listOf(20, 14), onLifeChange = { _, _ -> }, onMenu = {}) }
}

@Preview(widthDp = 320, heightDp = 533, showBackground = true)
@Composable
private fun GameCounterDarkPreview() {
    AppTheme(ThemeMode.Dark) { GameCounterScreen(lives = listOf(-3, 8000), onLifeChange = { _, _ -> }, onMenu = {}) }
}
