@file:OptIn(ExperimentalMaterial3Api::class)

package ua.ofkkt.navshowcase.crud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ua.ofkkt.navshowcase.data.Course
import ua.ofkkt.navshowcase.data.ScreenUiState
import ua.ofkkt.navshowcase.ui.common.AppCard
import ua.ofkkt.navshowcase.ui.common.LabeledSwitch
import ua.ofkkt.navshowcase.ui.common.NoInsets
import ua.ofkkt.navshowcase.ui.common.PrimaryButton
import ua.ofkkt.navshowcase.ui.common.UiStateContent

/**
 * ROUTE (stateful): знає про ViewModel і одноразові події; сам нічого не малює.
 * Лекція 9: «екран приймає стан і колбеки як параметри, але не створює їх самостійно».
 */
@Composable
fun CourseListRoute(
    viewModel: CrudViewModel,
    onOpen: (Int) -> Unit,
    onAdd: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val simulateError by viewModel.simulateError.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Одноразові повідомлення з ViewModel → Snackbar (з кнопкою «Повторити» для помилок оновлення)
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            val result = snackbarHostState.showSnackbar(
                message = message.text,
                actionLabel = if (message.canRetry) "Повторити" else null,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.load()
        }
    }

    CourseListScreen(
        state = state,
        isRefreshing = isRefreshing,
        simulateError = simulateError,
        snackbarHostState = snackbarHostState,
        viewModelId = viewModel.instanceId,
        onCourseClick = onOpen,
        onAdd = onAdd,
        onRefresh = { viewModel.load() },
        onRetry = { viewModel.load() },
        onSimulateErrorChange = { viewModel.setSimulateError(it) },
    )
}

/** SCREEN (stateless): усе, що потрібно для відображення, приходить параметрами. */
@Composable
fun CourseListScreen(
    state: ScreenUiState<List<Course>>,
    isRefreshing: Boolean,
    simulateError: Boolean,
    snackbarHostState: SnackbarHostState,
    viewModelId: Int,
    onCourseClick: (Int) -> Unit,
    onAdd: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onSimulateErrorChange: (Boolean) -> Unit,
) {
    Scaffold(
        contentWindowInsets = NoInsets,
        topBar = { TopAppBar(title = { Text("Курси") }, windowInsets = NoInsets) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Icon(Icons.Filled.Add, contentDescription = "Додати курс") }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { inner ->
        Column(Modifier.padding(inner)) {
            DemoControls(
                simulateError = simulateError,
                viewModelId = viewModelId,
                onSimulateErrorChange = onSimulateErrorChange,
                onOpenMissing = { onCourseClick(999) },
            )
            UiStateContent(
                state = state,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
                emptyTitle = "Курсів поки немає",
                emptyHint = "Натисніть «+», щоб додати перший курс",
                emptyAction = { PrimaryButton("Додати курс", onAdd, icon = Icons.Filled.Add) },
            ) { courses ->
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(courses, key = { it.id }) { course ->
                            AppCard(
                                title = course.title,
                                subtitle = course.teacher,
                                supporting = "${course.hours} год" + if (course.done) " · пройдено" else "",
                                leadingIcon = if (course.done) Icons.Filled.Check else Icons.Filled.Star,
                                onClick = { onCourseClick(course.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Допоміжна панель для демонстрації: керує станами, які важко викликати «природно». */
@Composable
private fun DemoControls(
    simulateError: Boolean,
    viewModelId: Int,
    onSimulateErrorChange: (Boolean) -> Unit,
    onOpenMissing: () -> Unit,
) {
    OutlinedCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            LabeledSwitch("Імітувати помилку мережі", simulateError, onSimulateErrorChange)
            TextButton(onClick = onOpenMissing) { Text("Відкрити неіснуючий курс #999") }
            Text(
                "CrudViewModel #$viewModelId — один на весь граф",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
