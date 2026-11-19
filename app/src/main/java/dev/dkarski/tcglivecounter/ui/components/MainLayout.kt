package dev.dkarski.tcglivecounter.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.RowScope
import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.nav_bar.NavigationBarItemDefaultsMMD
import com.mudita.mmd.components.nav_bar.NavigationBarItemMMD
import com.mudita.mmd.components.nav_bar.NavigationBarMMD
import com.mudita.mmd.components.text.TextMMD
import dev.dkarski.tcglivecounter.R
import dev.dkarski.tcglivecounter.ui.theme.muted

private val SelectedIndicatorHeight = 6.75.dp

private val IconOffset = 3.dp
private val LabelOffset = (-4.5).dp

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
                TabItem(Tab.Game, R.drawable.ic_nav_game, R.string.tab_game, selectedTab, onTabSelected)
                TabItem(Tab.History, R.drawable.ic_nav_history, R.string.tab_history, selectedTab, onTabSelected, comingSoon = true)
                TabItem(Tab.Settings, R.drawable.ic_nav_settings, R.string.tab_settings, selectedTab, onTabSelected)
            }
        },
        content = content,
    )
}

@Composable
private fun RowScope.TabItem(
    tab: Tab,
    @DrawableRes icon: Int,
    labelRes: Int,
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    comingSoon: Boolean = false,
) {
    val muted = MaterialTheme.colorScheme.muted
    val selected = tab == selectedTab
    val indicatorColor = MaterialTheme.colorScheme.onSurface
    NavigationBarItemMMD(
        selected = selected,
        
        // A thicker bar is drawn over it at the same place and colour.
        modifier = Modifier.drawWithContent {
            drawContent()
            if (selected) {
                val height = SelectedIndicatorHeight.toPx()
                drawRect(indicatorColor, topLeft = Offset(0f, size.height - height), size = Size(size.width, height))
            }
        },
        onClick = { onTabSelected(tab) },
        
        // Draw-time offsets only: the MMD item layout itself is unchanged.
        icon = { Icon(painterResource(icon), contentDescription = null, modifier = Modifier.offset(y = IconOffset)) },
        label = {
            TextMMD(
                text = stringResource(labelRes),
                fontWeight = if (selected) FontWeight.Black else null,
                modifier = Modifier.offset(y = LabelOffset),
            )
        },
        colors = if (comingSoon) {
            NavigationBarItemDefaultsMMD.colors(unselectedIconColor = muted, unselectedTextColor = muted)
        } else {
            NavigationBarItemDefaultsMMD.colors()
        },
    )
}
