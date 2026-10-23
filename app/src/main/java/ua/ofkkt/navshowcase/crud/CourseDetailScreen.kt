@file:OptIn(ExperimentalMaterial3Api::class)

package ua.ofkkt.navshowcase.crud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ua.ofkkt.navshowcase.data.Course
import ua.ofkkt.navshowcase.data.ScreenUiState
import ua.ofkkt.navshowcase.ui.common.EmptyState
import ua.ofkkt.navshowcase.ui.common.LabeledSwitch
import ua.ofkkt.navshowcase.ui.common.NoInsets
import ua.ofkkt.navshowcase.ui.common.PrimaryButton
import ua.ofkkt.navshowcase.ui.common.SecondaryButton

/** ROUTE: бере ID з аргументу маршруту і знаходить курс у стані спільного ViewModel. */
@Composable
fun CourseDetailRoute(
    courseId: Int,
    viewModel: CrudViewModel,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val current = state
    val course = if (current is ScreenUiState.Success) current.data.firstOrNull { it.id == courseId } else null

    // Пам'ятаємо останній відомий курс: після видалення екран не «блимає» повідомленням «не знайдено»,
    // поки програється анімація закриття.
    var lastKnown by remember { mutableStateOf<Course?>(null) }
    LaunchedEffect(course) { if (course != null) lastKnown = course }

    CourseDetailScreen(
        courseId = courseId,
        course = course ?: lastKnown,
        onBack = onBack,
        onEdit = { onEdit(courseId) },
        onToggleDone = { viewModel.toggleDone(courseId) },
        onDelete = {
            onBack()
            viewModel.delete(courseId)
        },
    )
}

/** SCREEN: course == null — це окремий UI-стан «не знайдено», а не збій застосунку. */
@Composable
fun CourseDetailScreen(
    courseId: Int,
    course: Course?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    if (course == null) {
        CourseNotFound(courseId = courseId, onBack = onBack)
        return
    }

    Scaffold(
        contentWindowInsets = NoInsets,
        topBar = {
            TopAppBar(
                title = { Text(course.title) },
                windowInsets = NoInsets,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Редагувати") }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Видалити")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier.padding(inner).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ListItem(overlineContent = { Text("Викладач") }, headlineContent = { Text(course.teacher) })
            ListItem(overlineContent = { Text("Обсяг") }, headlineContent = { Text("${course.hours} годин") })
            ListItem(overlineContent = { Text("ID з маршруту") }, headlineContent = { Text("#${course.id}") })
            LabeledSwitch("Курс пройдено", course.done, { onToggleDone() })
            PrimaryButton("Редагувати", onEdit, Modifier.fillMaxWidth(), icon = Icons.Filled.Edit)
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Видалити курс?") },
            text = { Text("«${course.title}» буде видалено зі списку.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Видалити") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Скасувати") }
            },
        )
    }
}

/**
 * Лекція 7, слайд «Приклад: граф для CRUD-застосунку»: помилка навігації (неіснуючий ID) —
 * окремий стан UI («Не знайдено»), а не крах застосунку.
 */
@Composable
fun CourseNotFound(courseId: Int, onBack: () -> Unit) {
    Scaffold(
        contentWindowInsets = NoInsets,
        topBar = {
            TopAppBar(
                title = { Text("Не знайдено") },
                windowInsets = NoInsets,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { inner ->
        EmptyState(
            title = "Курс #$courseId не знайдено",
            hint = "Можливо, його вже видалено або посилання застаріло.",
            modifier = Modifier.padding(inner),
            action = { SecondaryButton("До списку", onBack) },
        )
    }
}
