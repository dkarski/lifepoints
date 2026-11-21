package dev.dkarski.tcglivecounter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.buttons.ButtonDefaultsMMD
import com.mudita.mmd.components.text.TextMMD
import dev.dkarski.tcglivecounter.R
import dev.dkarski.tcglivecounter.data.GameState
import dev.dkarski.tcglivecounter.ui.components.CaptionTileHeight
import dev.dkarski.tcglivecounter.ui.components.ChoiceTile

/**
 * Table settings: starting life (20, 30, 40 or a custom value behind the pencil) and the number of players.
 * [life] is the value shown as selected (the current table, or the pending change).
 * Tapping the pencil replaces the row with a number field; a valid number becomes the selected life.
 */
@Composable
fun TableSettings(
    life: Int,
    onLifeSelect: (Int) -> Unit,
    onComingSoonPlayers: (Feature) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var editing by rememberSaveable { mutableStateOf(false) }
    val lifeUnit = stringResource(R.string.home_unit_life)
    val custom = life !in GameState.PRESETS

    // More space between the life row and the players row than inside a row: two separate choices.
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (editing) {
            CustomLifeField(
                initial = if (custom) life.toString() else "",
                onDone = { value ->
                    editing = false
                    value?.let(onLifeSelect)
                },
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameState.PRESETS.forEach { preset ->
                    ChoiceTile(
                        label = preset.toString(),
                        caption = lifeUnit,
                        selected = life == preset,
                        onClick = { focusManager.clearFocus(); onLifeSelect(preset) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // The pencil alone, or the custom value with a small pencil once one is set.
                ChoiceTile(
                    label = if (custom) life.toString() else "",
                    caption = if (custom) lifeUnit else null,
                    selected = custom,
                    icon = R.drawable.ic_edit,
                    description = if (custom) {
                        stringResource(R.string.custom_life_value, life)
                    } else {
                        stringResource(R.string.custom_life)
                    },
                    height = CaptionTileHeight,
                    onClick = { editing = true },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceTile(
                label = "2",
                caption = stringResource(R.string.home_unit_players),
                selected = true,
                onClick = { focusManager.clearFocus() },
                modifier = Modifier.weight(1f),
            )
            ChoiceTile(
                label = "3",
                selected = false,
                caption = stringResource(R.string.soon),
                unavailable = true,
                onClick = { focusManager.clearFocus(); onComingSoonPlayers(Feature.Players3) },
                modifier = Modifier.weight(1f),
            )
            ChoiceTile(
                label = "4",
                selected = false,
                caption = stringResource(R.string.soon),
                unavailable = true,
                onClick = { focusManager.clearFocus(); onComingSoonPlayers(Feature.Players4) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Number field that replaces the life row while a custom value is typed. Opens the keyboard immediately;
 * editing ends on Done or when the field loses focus. [onDone] gets the number, or null when it is empty or out
 * of range (the selection then stays as it was).
 * The caret is hidden (a blinking cursor would refresh the E Ink panel continuously); the selection starts
 * at the end, so digits append and Backspace deletes from the end.
 */
@Composable
private fun CustomLifeField(initial: String, onDone: (Int?) -> Unit) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var hadFocus by remember { mutableStateOf(false) }
    var fieldValue by remember { mutableStateOf(TextFieldValue(initial, TextRange(initial.length))) }
    val colors = MaterialTheme.colorScheme
    val textStyle = MaterialTheme.typography.titleMedium.copy(
        color = colors.onPrimaryContainer,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    val hint = stringResource(R.string.home_custom_edit)

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(CaptionTileHeight)
            .background(colors.primaryContainer, ButtonDefaultsMMD.shape)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            value = fieldValue,
            onValueChange = { new ->
                val digits = new.text.filter(Char::isDigit).take(4)
                fieldValue = TextFieldValue(digits, TextRange(digits.length))
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        hadFocus = true
                    } else if (hadFocus) {
                        hadFocus = false
                        onDone(
                            fieldValue.text.toIntOrNull()
                                ?.takeIf { it in GameState.MIN_STARTING_LIFE..GameState.MAX_STARTING_LIFE },
                        )
                    }
                }
                // A hardware Enter ends editing here and is consumed: otherwise its key-up reaches the element that
                // gets focus next (the top bar's Back arrow) and leaves Settings.
                .onPreviewKeyEvent { event ->
                    if (event.key != Key.Enter && event.key != Key.NumPadEnter) return@onPreviewKeyEvent false
                    if (event.type == KeyEventType.KeyUp) focusManager.clearFocus()
                    true
                }
                .semantics { contentDescription = hint },
            singleLine = true,
            textStyle = textStyle,
            cursorBrush = SolidColor(Color.Transparent),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.Center) {
                    if (fieldValue.text.isEmpty()) {
                        TextMMD(
                            text = hint,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onPrimaryContainer.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    inner()
                }
            },
        )
    }
}
