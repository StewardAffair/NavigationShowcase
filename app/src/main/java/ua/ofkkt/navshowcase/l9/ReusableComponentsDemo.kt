package ua.ofkkt.navshowcase.l9

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ua.ofkkt.navshowcase.ui.common.AppCard
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.EmptyState
import ua.ofkkt.navshowcase.ui.common.ErrorState
import ua.ofkkt.navshowcase.ui.common.LabeledSwitch
import ua.ofkkt.navshowcase.ui.common.LoadingIndicator
import ua.ofkkt.navshowcase.ui.common.PrimaryButton
import ua.ofkkt.navshowcase.ui.common.SecondaryButton
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 9 · слайд «Перевикористовувані компоненти»:
 * що виносити в компоненти, параметризація, власна бібліотека (AppCard, PrimaryButton, LoadingIndicator).
 * Код компонентів — ui/common/AppComponents.kt (ними користуються всі екрани цього застосунку).
 */
@Composable
fun ReusableComponentsDemo() {
    var padding by rememberSaveable { mutableStateOf(16f) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var withIcon by rememberSaveable { mutableStateOf(true) }
    var loading by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    DemoSection(
        "1. Параметризація: PrimaryButton",
        note = "Один компонент — багато станів: текст, іконка, enabled, loading задаються аргументами, нічого не захардкоджено всередині.",
    ) {
        LabeledSwitch("enabled", enabled, { enabled = it })
        LabeledSwitch("icon", withIcon, { withIcon = it })
        PrimaryButton(
            text = "Зберегти",
            onClick = {
                scope.launch {
                    loading = true
                    delay(1500)
                    loading = false
                }
            },
            enabled = enabled,
            loading = loading,
            icon = if (withIcon) Icons.Filled.Add else null,
        )
        Text("Натисніть кнопку — вона покаже стан loading 1,5 с.", style = MaterialTheme.typography.bodySmall)
        SecondaryButton("SecondaryButton", onClick = {}, enabled = enabled)
    }

    DemoSection(
        "2. Одна правка — усі картки",
        note = "Три картки — один AppCard. Змініть відступ повзунком: виправлення в одному місці, а не в п'яти.",
    ) {
        Slider(value = padding, onValueChange = { padding = it }, valueRange = 4f..32f)
        Text("contentPadding = ${padding.toInt()} dp")
        AppCard(
            title = "Kotlin: основи",
            subtitle = "О. Мельник",
            leadingIcon = Icons.Filled.Star,
            contentPadding = padding.dp,
        )
        AppCard(
            title = "Jetpack Compose",
            subtitle = "Р. Ключник",
            leadingIcon = Icons.Filled.Person,
            supporting = "32 год",
            contentPadding = padding.dp,
        )
        AppCard(
            title = "Із trailing-слотом",
            leadingIcon = Icons.Filled.Favorite,
            trailing = { Text("★ 4.8") },
            contentPadding = padding.dp,
            onClick = {},
        )
    }

    DemoSection(
        "3. Галерея станів-компонентів",
        note = "LoadingIndicator, ErrorState, EmptyState — компоненти, які знадобляться на кожному екрані з даними.",
    ) {
        LoadingIndicator()
        ErrorState(message = "Не вдалося завантажити дані", onRetry = {})
        EmptyState(title = "Тут порожньо", hint = "Додайте перший елемент")
    }

    DemoSection("4. Що виносити в компоненти") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Картки · кнопки · поля вводу · індикатори завантаження · заголовки секцій · порожні стани", style = MaterialTheme.typography.bodyMedium)
        }
        CodeSnippet(
            """
            // Власна бібліотека компонентів — пришвидшує розробку нових екранів
            // і гарантує консистентний стиль у всьому застосунку
            AppCard(title = course.title, subtitle = course.teacher, onClick = onOpen)
            PrimaryButton("Зберегти", onClick = onSave, loading = isSaving)
            LoadingIndicator()
            """,
        )
        Tip("Якщо ви пишете схожий код уже вдруге — час виносити його в composable. Картка товару в п'яти місцях = п'ять правок на кожен відступ.", TipKind.Warning)
    }
}
