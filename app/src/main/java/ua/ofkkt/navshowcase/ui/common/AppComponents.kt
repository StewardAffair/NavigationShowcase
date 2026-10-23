@file:OptIn(ExperimentalMaterial3Api::class)

package ua.ofkkt.navshowcase.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ua.ofkkt.navshowcase.data.ScreenUiState

/*
 * ─────────────────────────────────────────────────────────────────────────
 * Лекція 9, слайд «Перевикористовувані компоненти» — власна бібліотека:
 *   AppCard, PrimaryButton, LoadingIndicator (+ ErrorState, EmptyState, SectionHeader).
 * Кожен компонент параметризується аргументами (текст, іконка, колбек) і
 * НЕ містить хардкоду кольорів — лише MaterialTheme. Використовується в усіх екранах застосунку.
 * ─────────────────────────────────────────────────────────────────────────
 */

/** Універсальна картка: заголовок + підзаголовок + опційні іконка / trailing-слот / клік. */
@Composable
fun AppCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    supporting: String? = null,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
) {
    val body: @Composable () -> Unit = {
        Row(
            Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium)
                }
                if (supporting != null) {
                    Text(
                        supporting,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            trailing?.invoke()
        }
    }
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth()) { body() }
    } else {
        Card(modifier = modifier.fillMaxWidth()) { body() }
    }
}

/** Основна кнопка з опційною іконкою та станом «loading». */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled && !loading) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current,
            )
            Spacer(Modifier.width(8.dp))
        } else if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
}

/** Стан Loading: індикатор + підпис. */
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier, message: String = "Завантаження…") {
    Column(
        modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        CircularProgressIndicator()
        Text(message, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Стан Error: зрозуміле повідомлення + кнопка «Повторити». */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    retryLabel: String = "Повторити",
) {
    Column(
        modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text("⚠️", style = MaterialTheme.typography.displaySmall)
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error,
        )
        PrimaryButton(retryLabel, onRetry)
    }
}

/** Стан Empty: дружнє повідомлення + підказка про першу дію (а не помилка!). */
@Composable
fun EmptyState(
    title: String,
    hint: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Text("📭", style = MaterialTheme.typography.displaySmall)
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            hint,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        action?.invoke()
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

/**
 * Єдине місце, де для екрана з даними обробляються ВСІ UI-стани (Лекція 9).
 * Екран передає лише стан, колбек повтору та слот Success.
 */
@Composable
fun <T> UiStateContent(
    state: ScreenUiState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    emptyTitle: String = "Поки що нічого немає",
    emptyHint: String = "Додайте перший елемент",
    emptyAction: (@Composable () -> Unit)? = null,
    content: @Composable (T) -> Unit,
) {
    when (state) {
        ScreenUiState.Loading -> LoadingIndicator(modifier)
        is ScreenUiState.Error -> ErrorState(state.message, onRetry, modifier)
        ScreenUiState.Empty -> EmptyState(emptyTitle, emptyHint, modifier, emptyAction)
        is ScreenUiState.Success -> content(state.data)
    }
}
