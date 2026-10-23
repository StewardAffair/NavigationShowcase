package ua.ofkkt.navshowcase.crud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ua.ofkkt.navshowcase.data.NEW_COURSE_ID

/** Route-константи — в одному місці (Лекція 7: sealed class / object проти друкарських помилок). */
object CrudRoutes {
    const val GRAPH = "courses_graph"
    const val LIST = "courses"
    const val DETAIL = "detail/{courseId}"
    const val EDIT = "edit?courseId={courseId}"
    const val ARG_ID = "courseId"

    fun detail(id: Int) = "detail/$id"

    /** id == null → форма створення нового запису (аргумент отримає значення за замовчуванням −1). */
    fun edit(id: Int?) = if (id == null) "edit" else "edit?courseId=$id"
}

/**
 * Підсумковий мінізастосунок «Курси» — класична CRUD-схема з лекцій:
 *   Список → Деталі → Редагування → Збереження → (popBackStack до Списку).
 * Поєднує: NavHost, вкладений граф, аргументи, спільний ViewModel, Scaffold на кожному екрані,
 * Loading/Success/Error/Empty, Snackbar, pull-to-refresh, BackHandler, «не знайдено» як стан UI.
 */
@Composable
fun CrudApp() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = CrudRoutes.GRAPH) {
        navigation(startDestination = CrudRoutes.LIST, route = CrudRoutes.GRAPH) {
            composable(CrudRoutes.LIST) { entry ->
                val vm = crudViewModel(nav, entry)
                CourseListRoute(
                    viewModel = vm,
                    onOpen = { id -> nav.navigate(CrudRoutes.detail(id)) },
                    onAdd = { nav.navigate(CrudRoutes.edit(null)) },
                )
            }
            composable(
                route = CrudRoutes.DETAIL,
                arguments = listOf(navArgument(CrudRoutes.ARG_ID) { type = NavType.IntType }),
            ) { entry ->
                val vm = crudViewModel(nav, entry)
                CourseDetailRoute(
                    courseId = entry.arguments?.getInt(CrudRoutes.ARG_ID) ?: NEW_COURSE_ID,
                    viewModel = vm,
                    onBack = { nav.popBackStack() },
                    onEdit = { id -> nav.navigate(CrudRoutes.edit(id)) },
                )
            }
            composable(
                route = CrudRoutes.EDIT,
                arguments = listOf(
                    navArgument(CrudRoutes.ARG_ID) {
                        type = NavType.IntType
                        defaultValue = NEW_COURSE_ID
                    },
                ),
            ) { entry ->
                val vm = crudViewModel(nav, entry)
                CourseEditRoute(
                    courseId = entry.arguments?.getInt(CrudRoutes.ARG_ID) ?: NEW_COURSE_ID,
                    viewModel = vm,
                    // Після збереження — одразу до Списку, а не до Деталей (щоб не показувати застарілі дані)
                    onSaved = { nav.popBackStack(CrudRoutes.LIST, inclusive = false) },
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }
}

/** ViewModel, спільний для всього графа: власник — запис графа, а не поточного екрана. */
@Composable
private fun crudViewModel(nav: NavController, entry: NavBackStackEntry): CrudViewModel {
    val graphEntry = remember(entry) { nav.getBackStackEntry(CrudRoutes.GRAPH) }
    return viewModel<CrudViewModel>(graphEntry)
}
