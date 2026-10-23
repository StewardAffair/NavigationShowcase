package ua.ofkkt.navshowcase.l9

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ua.ofkkt.navshowcase.ui.common.ActionRow
import ua.ofkkt.navshowcase.ui.common.AppCard
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.CompareTable
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.ErrorState
import ua.ofkkt.navshowcase.ui.common.MiniScreen
import ua.ofkkt.navshowcase.ui.common.NoInsets
import ua.ofkkt.navshowcase.ui.common.SandboxFrame
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 9 · слайд «Обробка помилок мережі в UI»: Snackbar — неблокуюче повідомлення;
 * snackbarHostState.showSnackbar(message, actionLabel) — suspend, викликається з корутини;
 * таблиця «Коли блокувати екран, а коли ні».
 */
private enum class ErrorScreen { Content, FullError, Login }

@Composable
fun ErrorHandlingDemo() {
    var screen by rememberSaveable { mutableStateOf(ErrorScreen.FullError) }
    var name by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var log by rememberSaveable { mutableStateOf("—") }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    DemoSection(
        "1. Оберіть ситуацію",
        note = "Кожна кнопка відтворює один рядок таблиці зі слайда. Дивіться, як змінюється реакція UI.",
    ) {
        ActionRow(
            listOf(
                "Перше завантаження, даних немає" to {
                    nameError = null
                    screen = ErrorScreen.FullError
                },
                "Помилка оновлення, дані є" to {
                    nameError = null
                    screen = ErrorScreen.Content
                    scope.launch {
                        val result = snackbar.showSnackbar(
                            message = "Немає з'єднання, показані збережені дані",
                            actionLabel = "Повторити",
                            duration = SnackbarDuration.Long,
                        )
                        log = if (result == SnackbarResult.ActionPerformed) {
                            "Користувач натиснув «Повторити» → запит надсилається знову"
                        } else {
                            "Snackbar закрито без дії"
                        }
                    }
                },
                "Помилка відправки форми" to {
                    screen = ErrorScreen.Content
                    nameError = "Сервер відхилив ім'я: воно вже зайняте"
                },
                "Критична помилка автентифікації" to {
                    nameError = null
                    screen = ErrorScreen.Login
                },
            ),
        )
        SandboxFrame(height = 330.dp) {
            Scaffold(
                contentWindowInsets = NoInsets,
                snackbarHost = { SnackbarHost(snackbar) },
            ) { inner ->
                when (screen) {
                    ErrorScreen.FullError -> ErrorState(
                        message = "Не вдалося завантажити дані. Перевірте з'єднання й спробуйте ще раз.",
                        onRetry = { screen = ErrorScreen.Content },
                        modifier = Modifier.padding(inner),
                    )
                    ErrorScreen.Login -> MiniScreen(
                        title = "Екран входу",
                        subtitle = "редирект після критичної помилки",
                        actions = listOf("Увійти знову" to { screen = ErrorScreen.Content }),
                        modifier = Modifier.padding(inner),
                    )
                    ErrorScreen.Content -> Column(
                        Modifier.padding(inner).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AppCard(title = "Kotlin: основи", subtitle = "дані на екрані лишаються видимими")
                        AppCard(title = "Jetpack Compose", subtitle = "поверх них — лише Snackbar")
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; nameError = null },
                            label = { Text("Ім'я") },
                            isError = nameError != null,
                            supportingText = { nameError?.let { Text(it) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Button(onClick = { nameError = "Ім'я не може бути порожнім".takeIf { name.isBlank() } }) {
                            Text("Надіслати")
                        }
                    }
                }
            }
        }
        Text("Журнал: $log", style = MaterialTheme.typography.bodySmall)
    }

    DemoSection("2. Коли блокувати екран, а коли ні") {
        CompareTable(
            headers = listOf("Ситуація", "Рекомендація"),
            rows = listOf(
                listOf("Перше завантаження, даних немає", "Повноекранний Error-стан з кнопкою «Повторити»"),
                listOf("Помилка оновлення, дані є", "Snackbar + зберегти поточний контент"),
                listOf("Помилка відправки форми", "Inline-помилка під полем або Snackbar"),
                listOf("Критична помилка автентифікації", "Редирект на екран входу"),
            ),
        )
        Tip("«Немає з'єднання, показані збережені дані» — кращий UX за порожній екран з помилкою.", TipKind.Success)
    }

    DemoSection("3. Правила UX для помилок і код") {
        Text("• Не ховайте дані, що вже відображені, через помилку оновлення — показуйте Snackbar поверх.", style = MaterialTheme.typography.bodySmall)
        Text("• Завжди пропонуйте «Повторити» — користувач очікує цю можливість.", style = MaterialTheme.typography.bodySmall)
        Text("• Формулюйте зрозуміло: «Немає з'єднання» краще за «Network exception: timeout».", style = MaterialTheme.typography.bodySmall)
        CodeSnippet(
            """
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
                // showSnackbar — suspend-функція, тому виклик — з корутини
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "Немає з'єднання",
                        actionLabel = "Повторити",
                    )
                    if (result == SnackbarResult.ActionPerformed) onRetry()
                }
            }
            """,
        )
    }
}
