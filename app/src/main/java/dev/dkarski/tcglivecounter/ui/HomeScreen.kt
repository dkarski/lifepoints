package dev.dkarski.tcglivecounter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.buttons.ButtonDefaultsMMD
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.text.TextMMD
import dev.dkarski.tcglivecounter.R
import dev.dkarski.tcglivecounter.data.GameState
import dev.dkarski.tcglivecounter.ui.components.CaptionTileHeight
import dev.dkarski.tcglivecounter.ui.components.ChoiceTile
import dev.dkarski.tcglivecounter.ui.components.TileHeight
import dev.dkarski.tcglivecounter.ui.theme.AppTheme
import dev.dkarski.tcglivecounter.ui.theme.ThemeMode

/**
 * New Game configuration (content of the Main Layout).
 * [lifePreset] is the selected preset, or null when Custom is selected and [customText] applies.
 * Custom is edited inside its own tile, so the screen layout never changes height.
 */
@Composable
fun HomeScreen(
    lifePreset: Int?,
    customText: String,
    canStart: Boolean,
    onPresetSelect: (Int) -> Unit,
    onCustomSelect: () -> Unit,
    onCustomTextChange: (String) -> Unit,
    onComingSoonPlayers: (Feature) -> Unit,
    onStart: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val focusManager = LocalFocusManager.current
    var editingCustom by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            // Tapping empty space ends editing of the Custom value.
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val lifeUnit = stringResource(R.string.home_unit_life)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GameState.PRESETS.forEach { preset ->
                ChoiceTile(
                    label = preset.toString(),
                    caption = lifeUnit,
                    selected = lifePreset == preset,
                    onClick = { focusManager.clearFocus(); onPresetSelect(preset) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (editingCustom) {
            CustomLifeField(
                value = customText,
                onValueChange = { text -> onCustomTextChange(text.filter(Char::isDigit).take(4)) },
                onEditEnd = { editingCustom = false },
            )
        } else {
            val customValid = lifePreset == null && canStart
            ChoiceTile(
                label = if (customValid) customText else stringResource(R.string.home_custom),
                caption = lifeUnit,
                selected = lifePreset == null,
                onClick = { onCustomSelect(); editingCustom = true },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Groups are separated by space instead of section headings; each tile names its own unit.
        Spacer(Modifier.height(24.dp))
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

        Spacer(Modifier.weight(1f))
        ButtonMMD(
            onClick = { focusManager.clearFocus(); onStart() },
            enabled = canStart,
            modifier = Modifier.fillMaxWidth().height(TileHeight),
        ) {
            TextMMD(stringResource(R.string.home_start_game), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * The Custom tile in edit mode: same size and look as a selected tile, with a numeric field inside.
 * Opens the keyboard immediately; editing ends on Done or when the field loses focus.
 * The caret is hidden (a blinking cursor would refresh the E Ink panel continuously); the selection starts
 * at the end, so digits append and Backspace deletes from the end when the value is edited again.
 */
@Composable
private fun CustomLifeField(
    value: String,
    onValueChange: (String) -> Unit,
    onEditEnd: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var hadFocus by remember { mutableStateOf(false) }
    var fieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
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
                onValueChange(digits)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { state ->
                    if (state.isFocused) hadFocus = true else if (hadFocus) onEditEnd()
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

@Composable
internal fun SectionLabel(text: String) {
    TextMMD(text = text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
}

@Preview(widthDp = 360, heightDp = 520, showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AppTheme(ThemeMode.Light) {
        HomeScreen(20, "", true, {}, {}, {}, {}, {})
    }
}

@Preview(widthDp = 360, heightDp = 520, showBackground = true)
@Composable
private fun HomeScreenCustomDarkPreview() {
    AppTheme(ThemeMode.Dark) {
        androidx.compose.material3.Surface {
            HomeScreen(null, "25", true, {}, {}, {}, {}, {})
        }
    }
}
