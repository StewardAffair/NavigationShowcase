@file:OptIn(ExperimentalMaterial3Api::class)

package ua.ofkkt.navshowcase.l9

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ua.ofkkt.navshowcase.ui.common.ActionRow
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.CompareTable
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.LabeledSwitch
import ua.ofkkt.navshowcase.ui.common.NoInsets
import ua.ofkkt.navshowcase.ui.common.SandboxFrame
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 9 · слайд «Pull-to-refresh патерн»: PullToRefreshBox(isRefreshing, onRefresh) —
 * додаткове оновлення ПОВЕРХ наявного контенту, а не заміна Loading-стану.
 */
@Composable
fun PullToRefreshDemo() {
    var items by remember { mutableStateOf(listOf("Початкові дані #1", "Початкові дані #2", "Початкові дані #3")) }
    var isRefreshing by remember { mutableStateOf(false) }
    var failRefresh by remember { mutableStateOf(false) }
    var refreshNo by remember { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val refresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            delay(1500) // імітація запиту до джерела даних
            if (failRefresh) {
                // помилка оновлення: сховати індикатор, показати Snackbar, а попередні дані ЗАЛИШИТИ
                snackbar.showSnackbar("Не вдалося оновити. Показано попередні дані")
            } else {
                refreshNo++
                items = listOf("Оновлення #$refreshNo") + items
            }
            isRefreshing = false
        }
    }

    DemoSection(
        "1. Потягніть список вниз",
        note = "Дані, що вже відображені, лишаються на екрані — зверху з'являється лише індикатор оновлення.",
    ) {
        LabeledSwitch("Імітувати помилку оновлення", failRefresh, { failRefresh = it })
        ActionRow(listOf("Оновити програмно (isRefreshing = true)" to refresh))
        SandboxFrame(height = 320.dp) {
            Scaffold(
                contentWindowInsets = NoInsets,
                snackbarHost = { SnackbarHost(snackbar) },
            ) { inner ->
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = refresh,
                    modifier = Modifier.padding(inner).fillMaxSize(),
                ) {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(items, key = { it }) { text ->
                            ListItem(headlineContent = { Text(text) })
                        }
                    }
                }
            }
        }
        Tip("Pull-to-refresh — додаткове оновлення поверх наявного контенту, а не заміна Loading-стану. Після завершення контент замінюється новими даними без мигання.", TipKind.Info)
    }

    DemoSection("2. Інтеграція з UI-станами") {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("• isRefreshing: Boolean — true під час запиту, false після завершення (успіх чи помилка).", style = MaterialTheme.typography.bodySmall)
            Text("• onRefresh: () -> Unit — колбек повторного запиту; у блоці 4 це буде виклик методу ViewModel.", style = MaterialTheme.typography.bodySmall)
            Text("• Помилка при refresh — сховайте індикатор і покажіть Snackbar. Попередні дані лишаються видимими.", style = MaterialTheme.typography.bodySmall)
        }
        CompareTable(
            headers = listOf("", "Loading-стан", "Pull-to-refresh"),
            rows = listOf(
                listOf("Коли", "Даних ще немає", "Дані вже є"),
                listOf("Що бачить користувач", "Замість контенту — індикатор", "Контент + індикатор зверху"),
                listOf("Помилка", "Повноекранний Error", "Snackbar, дані збережені"),
            ),
        )
        CodeSnippet(
            """
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
            ) {
                LazyColumn { items(uiState.items) { ItemCard(it) } }
            }
            """,
        )
    }
}
