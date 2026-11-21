package dev.dkarski.tcglivecounter.ui

import android.os.SystemClock
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.text.TextMMD
import dev.dkarski.tcglivecounter.R
import dev.dkarski.tcglivecounter.data.LifeSeries
import dev.dkarski.tcglivecounter.ui.theme.AppTheme
import dev.dkarski.tcglivecounter.ui.theme.BorderWidth
import dev.dkarski.tcglivecounter.ui.theme.BottomPlayerColors
import dev.dkarski.tcglivecounter.ui.theme.MenuButtonColors
import dev.dkarski.tcglivecounter.ui.theme.PlayerColors
import dev.dkarski.tcglivecounter.ui.theme.TopPlayerColors
import kotlinx.coroutines.delay

/**
 * Fullscreen counter for two players facing each other across the table.
 * Player 1 sits at the bottom edge; Player 2's half is rotated 180° so it reads from the top edge.
 * Each half: the change indicator of the current series, then − life +. A tap changes life by 1, a hold by 10 per
 * step. [onLifeChange] applies a change and returns the applied amount (less at a life limit).
 */
@Composable
fun GameCounterScreen(
    lives: List<Int>,
    onLifeChange: (player: Int, delta: Int) -> Int,
    onMenu: () -> Unit,
    vibration: Boolean = true,
) {
    // Keep the screen awake only while the counter is visible (window flag, no permission or wake lock).
    // Leaving the counter, or the app, lets the system timeout work again.
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // The current series per player. Not saved: leaving the counter (menu, app closed) ends every series.
    val series = remember { mutableStateListOf<LifeSeries?>().apply { repeat(lives.size) { add(null) } } }
    val change: (player: Int, delta: Int, haptic: Haptic) -> Unit = { player, delta, haptic ->
        val applied = onLifeChange(player, delta)
        if (applied != 0) {
            series[player] = LifeSeries.record(series[player], applied, SystemClock.uptimeMillis())
            if (vibration) view.vibrate(haptic)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(Modifier.fillMaxSize()) {
            PlayerPanel(
                player = 1,
                colors = TopPlayerColors,
                life = lives[1],
                series = series[1],
                onSeriesEnd = { series[1] = null },
                onChange = { delta, haptic -> change(1, delta, haptic) },
                modifier = Modifier.weight(1f).rotate(180f),
            )
            PlayerPanel(
                player = 0,
                colors = BottomPlayerColors,
                life = lives[0],
                series = series[0],
                onSeriesEnd = { series[0] = null },
                onChange = { delta, haptic -> change(0, delta, haptic) },
                modifier = Modifier.weight(1f),
            )
        }
        MenuButton(onClick = onMenu, modifier = Modifier.align(Alignment.Center))
    }
}

private val IndicatorHeight = 44.dp
private val ControlsHeight = 140.dp
private val SideWidth = 76.dp

/** −, + and the menu button share one size; −/+ are only a visual hint, the touch area is the whole half. */
private val CircleSize = 56.dp

/** Numbers read left to right in every language (otherwise "−5" can show as "5−" in right-to-left languages). */
private val NumberStyle = TextStyle(textDirection = TextDirection.Ltr)

/**
 * One player's half, from their own perspective: the series indicator, then − life +.
 * Touch: the whole left half lowers life and the whole right half raises it; the circles only show where and when
 * (pressed state). [colors] is the half's fixed palette (by its position on the screen: white on top, black at the
 * bottom).
 */
@Composable
private fun PlayerPanel(
    player: Int,
    colors: PlayerColors,
    life: Int,
    series: LifeSeries?,
    onSeriesEnd: () -> Unit,
    onChange: (delta: Int, haptic: Haptic) -> Unit,
    modifier: Modifier = Modifier,
) {
    val name = stringResource(R.string.player_name, player + 1)
    // The series ends TIMEOUT_MS after its last change; a new change restarts the wait.
    if (series != null) {
        LaunchedEffect(series.lastChangeMs) {
            delay((series.lastChangeMs + LifeSeries.TIMEOUT_MS - SystemClock.uptimeMillis()).coerceAtLeast(0))
            onSeriesEnd()
        }
    }
    var minusPressed by remember { mutableStateOf(false) }
    var plusPressed by remember { mutableStateOf(false) }
    // Minus stays on the left and plus on the right for every player, also in right-to-left languages.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(modifier.background(colors.background).fillMaxWidth()) {
            // Touch layer: two halves of the player's field, each the full height.
            Row(Modifier.fillMaxSize()) {
                StepArea(
                    description = stringResource(R.string.counter_decrease, name),
                    holdDescription = stringResource(R.string.counter_decrease_10, name),
                    onTap = { onChange(-1, Haptic.Tap) },
                    onStep = { onChange(-10, Haptic.Step) },
                    onPressedChange = { minusPressed = it },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                StepArea(
                    description = stringResource(R.string.counter_increase, name),
                    holdDescription = stringResource(R.string.counter_increase_10, name),
                    onTap = { onChange(+1, Haptic.Tap) },
                    onStep = { onChange(+10, Haptic.Step) },
                    onPressedChange = { plusPressed = it },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
            // Drawing layer on top: no touch handling, so touches reach the halves underneath.
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Fixed slot: the indicator appearing or disappearing never moves the number or the buttons.
                Box(Modifier.fillMaxWidth().height(IndicatorHeight), contentAlignment = Alignment.Center) {
                    if (series != null) {
                        val text = formatChange(series.total)
                        val description = stringResource(R.string.counter_series, text)
                        TextMMD(
                            text = text,
                            style = NumberStyle,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.content,
                            modifier = Modifier.semantics { contentDescription = description },
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().height(ControlsHeight),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StepCircle(colors, R.drawable.ic_minus, minusPressed, Modifier.width(SideWidth))
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        val text = formatLife(life)
                        TextMMD(
                            text = text,
                            style = NumberStyle,
                            // Four characters ("−999", "9999") need a smaller size to fit between the circles.
                            fontSize = if (text.length >= 4) 72.sp else 96.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.content,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.wrapContentHeight(unbounded = true),
                        )
                    }
                    StepCircle(colors, R.drawable.ic_plus, plusPressed, Modifier.width(SideWidth))
                }
                Spacer(Modifier.height(IndicatorHeight)) // keeps the number in the middle of the half
            }
        }
    }
}

/** "+3", "−10", "0": the sign is always shown, with a real minus sign. */
internal fun formatChange(total: Int): String = when {
    total > 0 -> "+$total"
    total < 0 -> "\u2212${-total}"
    else -> "0"
}

internal fun formatLife(life: Int): String = if (life < 0) "\u2212${-life}" else life.toString()

/**
 * Touch area of − or + (half of the player's field). A tap runs [onTap], a hold runs [onStep] repeatedly
 * (see [handlePress]); [onPressedChange] drives the circle's pressed state. Each area has its own pointer input, so
 * both players (and both halves) can be pressed at the same time. Sliding into the other half cancels the press.
 * Leaving the counter cancels a running hold.
 */
@Composable
private fun StepArea(
    description: String,
    holdDescription: String,
    onTap: () -> Unit,
    onStep: () -> Unit,
    onPressedChange: (Boolean) -> Unit,
    modifier: Modifier,
) {
    val view = LocalView.current
    val holdMs = LocalViewConfiguration.current.longPressTimeoutMillis
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnStep by rememberUpdatedState(onStep)
    val currentOnPressedChange by rememberUpdatedState(onPressedChange)
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    currentOnPressedChange(true)
                    try {
                        handlePress(
                            holdMs = holdMs,
                            repeatMs = HOLD_REPEAT_MS,
                            waitForEnd = { timeout ->
                                withTimeoutOrNull(timeout) { awaitEnd(down.id) }
                                    // Still pressed, but the app lost focus (dialog, shade, background): stop.
                                    ?: if (view.hasWindowFocus()) null else PressEnd.Cancelled
                            },
                            onTap = { currentOnTap() },
                            onStep = { currentOnStep() },
                        )
                    } finally {
                        currentOnPressedChange(false)
                    }
                }
            }
            .semantics {
                role = Role.Button
                contentDescription = description
                onClick { currentOnTap(); true }
                // A hold is hard to do with a screen reader: ±10 is offered as an action.
                customActions = listOf(CustomAccessibilityAction(holdDescription) { currentOnStep(); true })
            },
    )
}

/**
 * The − or + circle. MMD has no pressed state, so the circle inverts within the player's [colors] while [pressed]
 * (filled with the content colour, symbol in the background colour) and for a short minimum time after a quick tap:
 * one static redraw of the circle only (no ripple, no animation), visible even when the E Ink panel lags. The life
 * total and the half never invert.
 */
@Composable
private fun StepCircle(colors: PlayerColors, @DrawableRes icon: Int, pressed: Boolean, modifier: Modifier) {
    var highlighted by remember { mutableStateOf(false) }
    LaunchedEffect(pressed) {
        if (pressed) {
            highlighted = true
        } else if (highlighted) {
            delay(PRESS_FEEDBACK_MS)
            highlighted = false
        }
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(CircleSize)
                .clip(CircleShape)
                .background(if (highlighted) colors.content else colors.background)
                .border(BorderWidth, colors.content, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(icon),
                contentDescription = null,
                tint = if (highlighted) colors.background else colors.content,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

/** Waits until the pointer [id] goes up (Released) or leaves the touch area or disappears (Cancelled). */
private suspend fun AwaitPointerEventScope.awaitEnd(id: PointerId): PressEnd {
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.firstOrNull { it.id == id } ?: return PressEnd.Cancelled
        if (change.changedToUpIgnoreConsumed()) return PressEnd.Released
        if (change.isOutOfBounds(size, extendedTouchPadding)) return PressEnd.Cancelled
        change.consume()
    }
}

/** Minimum time the pressed state stays visible after a quick tap. */
private const val PRESS_FEEDBACK_MS = 150L

@Composable
private fun MenuButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(CircleSize)
            .clip(CircleShape)
            .background(MenuButtonColors.background)
            .border(BorderWidth, MenuButtonColors.content, CircleShape)
            .clickable(onClick = onClick, interactionSource = null, indication = null),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = stringResource(R.string.counter_open_menu),
            tint = MenuButtonColors.content,
        )
    }
}

@Preview(widthDp = 360, heightDp = 600, showBackground = true)
@Composable
private fun GameCounterPreview() {
    AppTheme { GameCounterScreen(lives = listOf(20, 14), onLifeChange = { _, d -> d }, onMenu = {}) }
}

@Preview(widthDp = 360, heightDp = 600, showBackground = true)
@Composable
private fun GameCounterLimitsPreview() {
    AppTheme { GameCounterScreen(lives = listOf(-999, 9999), onLifeChange = { _, d -> d }, onMenu = {}) }
}
