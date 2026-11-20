package dev.dkarski.tcglivecounter.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.lazy.LazyColumnMMD
import com.mudita.mmd.components.radio_button.RadioButtonMMD
import com.mudita.mmd.components.text.TextMMD
import dev.dkarski.tcglivecounter.R
import dev.dkarski.tcglivecounter.data.AppLanguage
import dev.dkarski.tcglivecounter.data.AppLanguages
import dev.dkarski.tcglivecounter.ui.components.DottedDivider
import dev.dkarski.tcglivecounter.ui.components.SecondaryLayout
import dev.dkarski.tcglivecounter.ui.theme.AppTheme
import dev.dkarski.tcglivecounter.ui.theme.ThemeMode

/**
 * Settings > Language, modelled on the system Settings > System Language screen: one line per language,
 * radio button, each language under its own name. Order: System default, the 9 MuditaOS system languages
 * (system order), then "More languages" grouped by script. The search icon filters by own name, English name,
 * name in the current UI language and tag.
 *
 * [selectedTag] null = System default. [onSelect] receives null for System default.
 */
@Composable
fun LanguageScreen(
    selectedTag: String?,
    onSelect: (String?) -> Unit,
    onBack: () -> Unit,
) {
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val closeSearch = {
        searching = false
        query = ""
    }
    BackHandler(enabled = searching, onBack = closeSearch)

    SecondaryLayout(
        title = stringResource(R.string.settings_language),
        onBack = if (searching) closeSearch else onBack,
        titleContent = if (searching) {
            { SearchField(query = query, onQueryChange = { query = it }) }
        } else {
            null
        },
        actions = {
            if (!searching) {
                IconButton(onClick = { searching = true }) {
                    Icon(painterResource(R.drawable.ic_search), contentDescription = stringResource(R.string.language_search))
                }
            } else if (query.isNotEmpty()) {
                IconButton(onClick = { query = "" }) {
                    Icon(painterResource(R.drawable.ic_close), contentDescription = stringResource(R.string.language_clear_search))
                }
            }
        },
    ) { padding ->
        val uiLocale = LocalConfiguration.current.locales[0]
        val results = remember(query, uiLocale) { if (searching) AppLanguages.search(query, uiLocale) else emptyList() }
        LazyColumnMMD(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
            scrollStep = SCROLL_STEP,
        ) {
            when {
                !searching || query.isBlank() -> fullList(selectedTag, onSelect)
                results.isEmpty() -> item {
                    TextMMD(
                        text = stringResource(R.string.language_no_results),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                else -> items(results, key = { it.tag }) { lang ->
                    LanguageRow(lang.nativeName, selected = lang.tag == selectedTag) { onSelect(lang.tag) }
                }
            }
        }
    }
}

private fun LazyListScope.fullList(selectedTag: String?, onSelect: (String?) -> Unit) {
    item(key = "system-default") {
        LanguageRowWithLabel(R.string.language_system_default, selected = selectedTag == null) { onSelect(null) }
    }
    items(AppLanguages.system, key = { it.tag }) { lang -> LanguageItem(lang, selectedTag, onSelect) }
    item(key = "more-header") {
        TextMMD(
            text = stringResource(R.string.language_more),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
        )
    }
    items(AppLanguages.more, key = { it.tag }) { lang -> LanguageItem(lang, selectedTag, onSelect) }
}

@Composable
private fun LanguageItem(lang: AppLanguage, selectedTag: String?, onSelect: (String?) -> Unit) =
    LanguageRow(lang.nativeName, selected = lang.tag == selectedTag) { onSelect(lang.tag) }

@Composable
private fun LanguageRowWithLabel(label: Int, selected: Boolean, onClick: () -> Unit) =
    LanguageRow(stringResource(label), selected, onClick)

/** One line: radio button, name, dotted divider under the text (as in the system language screen). */
@Composable
private fun LanguageRow(name: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.RadioButton, interactionSource = null, indication = null),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButtonMMD(selected = selected, onClick = null)
            Spacer(Modifier.width(16.dp))
            TextMMD(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        }
        DottedDivider(Modifier.padding(start = 56.dp))
    }
}

/** Search field in the top bar. No visible cursor: a blinking cursor would keep refreshing the E Ink panel. */
@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = style,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.surface),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        decorationBox = { inner ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    TextMMD(
                        text = stringResource(R.string.language_search_hint),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                inner()
            }
        },
    )
}

/** About one screen of rows per swipe (E Ink jump scrolling). */
private const val SCROLL_STEP = 9

@Preview(widthDp = 360, heightDp = 600, showBackground = true)
@Composable
private fun LanguageScreenPreview() {
    AppTheme(ThemeMode.Light) { LanguageScreen(selectedTag = null, onSelect = {}, onBack = {}) }
}
