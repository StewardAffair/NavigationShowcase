@file:OptIn(ExperimentalMaterial3Api::class)

package ua.ofkkt.navshowcase.crud

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ua.ofkkt.navshowcase.data.Course
import ua.ofkkt.navshowcase.data.NEW_COURSE_ID
import ua.ofkkt.navshowcase.data.ScreenUiState
import ua.ofkkt.navshowcase.ui.common.NoInsets
import ua.ofkkt.navshowcase.ui.common.PrimaryButton

/** ROUTE: courseId == -1 (значення за замовчуванням аргументу) означає «створення нового курсу». */
@Composable
fun CourseEditRoute(
    courseId: Int,
    viewModel: CrudViewModel,
    onSaved: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val current = state
    val isNew = courseId == NEW_COURSE_ID
    val existing = if (current is ScreenUiState.Success) current.data.firstOrNull { it.id == courseId } else null

    if (!isNew && existing == null) {
        CourseNotFound(courseId = courseId, onBack = onBack)
    } else {
        CourseEditScreen(
            initial = existing,
            isNew = isNew,
            onSave = { course ->
                viewModel.save(course)
                onSaved()
            },
            onBack = onBack,
        )
    }
}

/**
 * SCREEN: форма з inline-валідацією та BackHandler, який активний лише за наявності незбережених змін
 * (Лекція 9: «Обробка системної кнопки Назад», «Помилка відправки форми → inline-помилка під полем»).
 */
@Composable
fun CourseEditScreen(
    initial: Course?,
    isNew: Boolean,
    onSave: (Course) -> Unit,
    onBack: () -> Unit,
) {
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var teacher by rememberSaveable { mutableStateOf(initial?.teacher ?: "") }
    var hours by rememberSaveable { mutableStateOf(initial?.hours?.toString() ?: "") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var showDiscard by remember { mutableStateOf(false) }

    val dirty = title != (initial?.title ?: "") ||
        teacher != (initial?.teacher ?: "") ||
        hours != (initial?.hours?.toString() ?: "")
    val hoursValue = hours.toIntOrNull()
    val titleError = if (submitted && title.isBlank()) "Назва обов'язкова" else null
    val hoursError = if (submitted && (hoursValue == null || hoursValue !in 1..500)) "Введіть число від 1 до 500" else null

    BackHandler(enabled = dirty) { showDiscard = true }

    val requestBack: () -> Unit = {
        if (dirty) {
            showDiscard = true
        } else {
            onBack()
        }
    }
    val saveClick: () -> Unit = {
        submitted = true
        if (title.isNotBlank() && hoursValue != null && hoursValue in 1..500) {
            onSave(
                Course(
                    id = initial?.id ?: NEW_COURSE_ID,
                    title = title.trim(),
                    teacher = teacher.trim().ifBlank { "—" },
                    hours = hoursValue,
                    done = initial?.done ?: false,
                ),
            )
        }
    }

    Scaffold(
        contentWindowInsets = NoInsets,
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Новий курс" else "Редагування") },
                windowInsets = NoInsets,
                navigationIcon = {
                    IconButton(onClick = requestBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Назва") },
                isError = titleError != null,
                supportingText = { titleError?.let { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = teacher,
                onValueChange = { teacher = it },
                label = { Text("Викладач") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = hours,
                onValueChange = { hours = it.filter { c -> c.isDigit() }.take(3) },
                label = { Text("Годин") },
                isError = hoursError != null,
                supportingText = { hoursError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            PrimaryButton("Зберегти", saveClick, Modifier.fillMaxWidth(), icon = Icons.Filled.Check)
            Text(
                if (dirty) "● Є незбережені зміни: системна «Назад» покаже діалог" else "Змін немає: «Назад» працює як зазвичай",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showDiscard) {
        AlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text("Відхилити зміни?") },
            text = { Text("Введені дані не буде збережено.") },
            confirmButton = {
                TextButton(onClick = { showDiscard = false; onBack() }) { Text("Відхилити") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscard = false }) { Text("Лишитися") }
            },
        )
    }
}
