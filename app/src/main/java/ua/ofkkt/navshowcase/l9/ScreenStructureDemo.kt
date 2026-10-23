package ua.ofkkt.navshowcase.l9

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.ofkkt.navshowcase.data.Course
import ua.ofkkt.navshowcase.data.SampleData
import ua.ofkkt.navshowcase.ui.common.AppCard
import ua.ofkkt.navshowcase.ui.common.ChoiceChips
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.EmptyState
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 9 · слайд «Структурування composable-екранів»:
 * кожен екран — окрема composable верхнього рівня; приймає стан і колбеки ЯК ПАРАМЕТРИ,
 * але не створює їх сам. Великий екран ділиться на ListHeader, ItemCard, EmptyPlaceholder.
 */
private enum class DataSource(val label: String) {
    ViewModel("Як із ViewModel"),
    Test("Тестові дані"),
    Empty("Порожньо"),
}

@Composable
fun ScreenStructureDemo() {
    var source by rememberSaveable { mutableStateOf(DataSource.ViewModel) }
    var refreshCount by rememberSaveable { mutableStateOf(0) }
    var lastClicked by rememberSaveable { mutableStateOf("—") }

    val items = when (source) {
        DataSource.ViewModel -> SampleData.courses.take(4)
        DataSource.Test -> listOf(Course(100, "Тестовий курс A", "Тест", 1), Course(101, "Тестовий курс B", "Тест", 2))
        DataSource.Empty -> emptyList()
    }

    DemoSection(
        "1. ListScreen не знає, звідки прийшли items",
        note = "Той самий екран живиться з трьох джерел — ні рядка в самому екрані не змінюється. Саме тому його легко тестувати й показувати в Preview.",
    ) {
        ChoiceChips(DataSource.entries, source, { source = it }, { it.label })
        ListScreen(
            items = items,
            onItemClick = { lastClicked = it.title },
            onRefresh = { refreshCount++ },
        )
        Text("onItemClick → $lastClicked · onRefresh викликано $refreshCount раз(ів)", style = MaterialTheme.typography.bodySmall)
    }

    DemoSection("2. Типова сигнатура екрана") {
        CodeSnippet(
            """
            @Composable
            fun ListScreen(
                items: List<Item>,
                onItemClick: (Item) -> Unit,
                onRefresh: () -> Unit,
            )
            """,
        )
        Text("Чому це важливо:", style = MaterialTheme.typography.labelLarge)
        Text("• легко замінити джерело даних без зміни UI", style = MaterialTheme.typography.bodySmall)
        Text("• простий preview у Android Studio", style = MaterialTheme.typography.bodySmall)
        Text("• тести не потребують реального ViewModel", style = MaterialTheme.typography.bodySmall)
        Text("• чіткий контракт між логікою та відображенням", style = MaterialTheme.typography.bodySmall)
    }

    DemoSection("3. Файлова структура") {
        CodeSnippet(
            """
            feature/list/
                ListScreen.kt        ← stateless: лише відображення (параметри + колбеки)
                ListRoute.kt         ← stateful: бере ViewModel / NavController, викликає ListScreen
                components/
                    ListHeader.kt
                    ItemCard.kt
                    EmptyPlaceholder.kt
            """,
        )
        Tip(
            "Типово один екран — один файл або пакет. Приклад Route + Screen дивіться в підсумковому застосунку (crud/CourseListScreen.kt).",
            TipKind.Success,
        )
    }
}

/** Stateless-екран: усе, що йому потрібно, приходить параметрами. */
@Composable
private fun ListScreen(
    items: List<Course>,
    onItemClick: (Course) -> Unit,
    onRefresh: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ListHeader(count = items.size, onRefresh = onRefresh)
        if (items.isEmpty()) {
            EmptyPlaceholder()
        } else {
            items.forEach { course -> ItemCard(course, onItemClick) }
        }
    }
}

@Composable
private fun ListHeader(count: Int, onRefresh: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Курсів: $count", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
        TextButton(onClick = onRefresh) { Text("Оновити") }
    }
}

@Composable
private fun ItemCard(course: Course, onClick: (Course) -> Unit) {
    AppCard(
        title = course.title,
        subtitle = course.teacher,
        supporting = "${course.hours} год",
        onClick = { onClick(course) },
    )
}

@Composable
private fun EmptyPlaceholder() {
    EmptyState(title = "Курсів поки немає", hint = "Натисніть «Оновити» або додайте перший курс")
}
