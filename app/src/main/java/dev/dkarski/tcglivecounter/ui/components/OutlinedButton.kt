package dev.dkarski.tcglivecounter.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mudita.mmd.components.buttons.ButtonDefaultsMMD
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import dev.dkarski.tcglivecounter.ui.theme.outlineBorder

/** [OutlinedButtonMMD] with the border from the app's colours (MMD's own border colour is hard-coded). */
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
