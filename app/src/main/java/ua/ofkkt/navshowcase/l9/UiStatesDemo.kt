package ua.ofkkt.navshowcase.l9

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ua.ofkkt.navshowcase.data.Course
import ua.ofkkt.navshowcase.data.SampleData
import ua.ofkkt.navshowcase.data.ScreenUiState
import ua.ofkkt.navshowcase.ui.common.AppCard
import ua.ofkkt.navshowcase.ui.common.ChoiceChips
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.CompareTable
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.PrimaryButton
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind
import ua.ofkkt.navshowcase.ui.common.UiStateContent

/**
 * Лекція 9 · слайд «Стани екрана: Loading / Success / Error» (+ Empty) і
 * «Типові помилки»: відсутність Loading/Error станів = «замерзлий» UI.
 */
private enum class Outcome(val label: String) {
    Success("Успіх"),
    Error("Помилка"),
    Empty("Порожньо"),
}

@Composable
fun UiStatesDemo() {
    var state by remember { mutableStateOf<ScreenUiState<List<Course>>>(ScreenUiState.Loading) }
    var outcome by rememberSaveable { mutableStateOf(Outcome.Success) }
    val scope = rememberCoroutineScope()

    DemoSection(
        "1. Один екран — чотири стани",
        note = "Оберіть результат «запиту» і натисніть «Завантажити»: екран пройде Loading → (Success | Error | Empty).",
    ) {
        ChoiceChips(Outcome.entries, outcome, { outcome = it }, { it.label })
        PrimaryButton(
            text = "Завантажити",
            onClick = {
                scope.launch {
                    state = ScreenUiState.Loading
                    delay(1500)
                    state = when (outcome) {
                        Outcome.Success -> ScreenUiState.Success(SampleData.courses.take(3))
                        Outcome.Error -> ScreenUiState.Error("Немає з'єднання з сервером")
                        Outcome.Empty -> ScreenUiState.Empty
                    }
                }
            },
            loading = state is ScreenUiState.Loading,
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            UiStateContent(
                state = state,
                onRetry = { outcome = Outcome.Success },
                emptyTitle = "Курсів поки немає",
                emptyHint = "Це не помилка — просто ще нічого не додано",
            ) { courses ->
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    courses.forEach { AppCard(title = it.title, subtitle = it.teacher) }
                }
            }
        }
        Text(
            "Поточний стан: ${state::class.simpleName}",
            style = MaterialTheme.typography.labelLarge,
        )
    }

    DemoSection("2. Що бачить користувач") {
        CompareTable(
            headers = listOf("Стан", "Що бачить користувач", "Реалізація"),
            rows = listOf(
                listOf("⏳ Loading", "Індикатор завантаження або skeleton", "CircularProgressIndicator() / shimmer"),
                listOf("✅ Success", "Реальні дані, повна функціональність", "LazyColumn тощо"),
                listOf("❌ Error", "Повідомлення + кнопка «Повторити»", "Text + Button(onClick = onRetry)"),
                listOf("📭 Empty", "Дружнє повідомлення, що список порожній", "Ілюстрація + підказка про першу дію"),
            ),
        )
        Tip("Порожній стан — окремий випадок: «Поки що нічого немає» ≠ помилка. Тому Empty моделюють окремо, а не як Success з порожнім списком.", TipKind.Info)
    }

    DemoSection("3. Моделювання у коді") {
        CodeSnippet(
            """
            sealed interface ScreenUiState<out T> {
                data object Loading : ScreenUiState<Nothing>
                data class Success<T>(val data: T) : ScreenUiState<T>
                data class Error(val message: String) : ScreenUiState<Nothing>
                data object Empty : ScreenUiState<Nothing>
            }

            when (uiState) {
                Loading -> LoadingIndicator()
                is Success -> CourseList(uiState.data)
                is Error -> ErrorState(uiState.message, onRetry)
                Empty -> EmptyState("Курсів поки немає", "Додайте перший")
            }
            """,
        )
        Tip("Типові наслідки відсутності цих станів: «замерзлий» UI при повільній мережі, порожній екран без пояснень, відсутність можливості повторити запит.", TipKind.Warning)
    }
}
