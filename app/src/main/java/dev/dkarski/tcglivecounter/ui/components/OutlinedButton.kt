package dev.dkarski.tcglivecounter.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mudita.mmd.components.buttons.ButtonDefaultsMMD
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import dev.dkarski.tcglivecounter.ui.theme.outlineBorder

/** [OutlinedButtonMMD] whose border follows the theme, so it stays visible in Dark mode. */
@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaultsMMD.contentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButtonMMD(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        border = outlineBorder(muted = !enabled),
        contentPadding = contentPadding,
        content = content,
    )
}
