package dev.dkarski.tcglivecounter.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.mudita.mmd.components.nav_bar.NavigationBarItemDefaultsMMD
import com.mudita.mmd.components.nav_bar.NavigationBarItemMMD
import com.mudita.mmd.components.nav_bar.NavigationBarMMD
import com.mudita.mmd.components.text.TextMMD
import dev.dkarski.tcglivecounter.R
import dev.dkarski.tcglivecounter.ui.theme.muted

/** Bottom navigation destinations of the Main Layout. */
enum class Tab { Game, History, Settings }

/**
 * Main Layout: content + bottom navigation (Game / History / Settings).
 * The bottom navigation is the only way between sections: no top app bar, no back arrow.
 * History is not built yet, so it is shown muted but still opens the Coming Soon screen.
 */
@Composable
fun MainLayout(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBarMMD {
                TabItem(Tab.Game, Icons.Filled.PlayArrow, R.string.tab_game, selectedTab, onTabSelected)
                TabItem(Tab.History, Icons.Filled.DateRange, R.string.tab_history, selectedTab, onTabSelected, comingSoon = true)
                TabItem(Tab.Settings, Icons.Filled.Settings, R.string.tab_settings, selectedTab, onTabSelected)
            }
        },
        content = content,
    )
}

@Composable
private fun RowScope.TabItem(
    tab: Tab,
    icon: ImageVector,
    labelRes: Int,
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    comingSoon: Boolean = false,
) {
    val muted = MaterialTheme.colorScheme.muted
    NavigationBarItemMMD(
        selected = tab == selectedTab,
        onClick = { onTabSelected(tab) },
        icon = { Icon(icon, contentDescription = null) },
        label = { TextMMD(stringResource(labelRes)) },
        colors = if (comingSoon) {
            NavigationBarItemDefaultsMMD.colors(unselectedIconColor = muted, unselectedTextColor = muted)
        } else {
            NavigationBarItemDefaultsMMD.colors()
        },
    )
}
