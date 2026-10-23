package ua.ofkkt.navshowcase.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Лекція 9, чек-лист скелета: «Консистентна тема».
 * MaterialTheme застосована на рівні кореневого composable і охоплює весь застосунок,
 * включно з темним режимом. Жодного хардкоду кольорів в екранах — лише MaterialTheme.colorScheme.*
 */
@Composable
fun NavShowcaseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
