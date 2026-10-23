package ua.ofkkt.navshowcase.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import ua.ofkkt.navshowcase.crud.CrudApp
import ua.ofkkt.navshowcase.l7.ArgumentsDemo
import ua.ofkkt.navshowcase.l7.BackStackDemo
import ua.ofkkt.navshowcase.l7.DeepLinksDemo
import ua.ofkkt.navshowcase.l7.NavBasicsDemo
import ua.ofkkt.navshowcase.l7.NavPlanningDemo
import ua.ofkkt.navshowcase.l7.NestedGraphsDemo
import ua.ofkkt.navshowcase.l7.ProcessDeathDemo
import ua.ofkkt.navshowcase.l7.SingleActivityDemo
import ua.ofkkt.navshowcase.l7.TopLevelPatternsDemo
import ua.ofkkt.navshowcase.l9.AdaptiveLayoutsDemo
import ua.ofkkt.navshowcase.l9.BackHandlerDemo
import ua.ofkkt.navshowcase.l9.ErrorHandlingDemo
import ua.ofkkt.navshowcase.l9.PullToRefreshDemo
import ua.ofkkt.navshowcase.l9.ReusableComponentsDemo
import ua.ofkkt.navshowcase.l9.ScaffoldSlotsDemo
import ua.ofkkt.navshowcase.l9.ScreenStructureDemo
import ua.ofkkt.navshowcase.l9.SharedViewModelDemo
import ua.ofkkt.navshowcase.l9.SkeletonChecklistDemo
import ua.ofkkt.navshowcase.l9.StateHoistingDemo
import ua.ofkkt.navshowcase.l9.UiStatesDemo

object Routes {
    const val HUB = "hub"
    const val DEEP_TARGET = "item/{id}"
    fun deepTarget(id: Int) = "item/$id"
}

enum class Lecture(val label: String, val title: String) {
    L7("Лекція 7", "Навігація (огляд), структура екранів"),
    L9("Лекція 9", "Побудова багатоекранних застосунків"),
    Capstone("Підсумок", "Усе разом — міні-застосунок"),
}

/**
 * Один запис каталогу = один екран-демо = один destination у кореневому NavHost.
 * slides — номери й назви слайдів лекції, які демонструє екран; tryIt — що саме натиснути.
 */
class DemoItem(
    val route: String,
    val title: String,
    val summary: String,
    val lecture: Lecture,
    val slides: List<String>,
    val tryIt: String,
    val icon: ImageVector,
    /** true — екран складається з секцій і прокручується; false — власний вертикальний layout (на весь екран). */
    val scrollable: Boolean = true,
    val content: @Composable () -> Unit,
)

val Demos: List<DemoItem> = listOf(
    // ───────────────────────────── Лекція 7 ─────────────────────────────
    DemoItem(
        route = "single_activity",
        title = "Single-Activity vs Multi-Activity",
        summary = "Одна точка входу замість Activity на кожен екран",
        lecture = Lecture.L7,
        slides = listOf("Слайд 4 · Single-Activity парадигма", "Слайд 13 · Single-Activity vs Multi-Activity"),
        tryIt = "натисніть кнопку «startActivity» — відкриється окремий Activity зі своїм lifecycle; порівняйте з рештою застосунку.",
        icon = Icons.Filled.Home,
        content = { SingleActivityDemo() },
    ),
    DemoItem(
        route = "nav_basics",
        title = "NavHost, NavController, destinations",
        summary = "Граф Список → Деталі → Редагувати, navigate / popBackStack",
        lecture = Lecture.L7,
        slides = listOf("Слайд 5 · Навігаційний граф: концепція", "Слайд 6 · NavHost та NavController", "Слайд 7 · composable() destinations"),
        tryIt = "ходіть по екранах у рамці й стежте за стеком; керуйте тим самим NavController кнопками ззовні.",
        icon = Icons.Filled.Place,
        content = { NavBasicsDemo() },
    ),
    DemoItem(
        route = "nav_arguments",
        title = "Аргументи навігації",
        summary = "Шлях {id}, NavType, query-параметри, defaultValue",
        lecture = Lecture.L7,
        slides = listOf("Слайд 8 · Передача аргументів між екранами"),
        tryIt = "змініть query/limit і перейдіть; натисніть navigate(\"user/abc\") — побачите, чому типи важливі.",
        icon = Icons.Filled.Share,
        content = { ArgumentsDemo() },
    ),
    DemoItem(
        route = "back_stack",
        title = "Back stack і popUpTo",
        summary = "LIFO, popUpTo(inclusive), launchSingleTop, popBackStack",
        lecture = Lecture.L7,
        slides = listOf("Слайд 9 · Back stack: як це працює"),
        tryIt = "пройдіть A→B→C→D і порівняйте popUpTo з inclusive=false/true; двічі натисніть navigate(c) зі та без launchSingleTop.",
        icon = Icons.Filled.Menu,
        content = { BackStackDemo() },
    ),
    DemoItem(
        route = "deep_links",
        title = "Deep links",
        summary = "navDeepLink, uriPattern, справжнє посилання через Intent",
        lecture = Lecture.L7,
        slides = listOf("Слайд 10 · Deep links: огляд"),
        tryIt = "введіть URI й перейдіть; потім надішліть справжній ACTION_VIEW — система відкриє окремий екран цього застосунку.",
        icon = Icons.Filled.Search,
        content = { DeepLinksDemo() },
    ),
    DemoItem(
        route = "nested_graphs",
        title = "Вкладені графи",
        summary = "auth-граф і main-граф, popUpTo з inclusive після входу",
        lecture = Lecture.L7,
        slides = listOf("Слайд 12 · Вкладені графи (nested graphs)"),
        tryIt = "натисніть «Увійти», потім системну «Назад» — на логін уже не повернутися; увімкніть показ записів графів у стеку.",
        icon = Icons.Filled.Build,
        content = { NestedGraphsDemo() },
    ),
    DemoItem(
        route = "top_level_patterns",
        title = "Bottom Navigation і Drawer",
        summary = "NavigationBar, ModalNavigationDrawer, saveState / restoreState",
        lecture = Lecture.L7,
        slides = listOf("Слайд 11 · Патерни навігації: Bottom Nav, Drawer", "Л9, слайд 10 · TopAppBar та BottomBar"),
        tryIt = "натискайте лічильник на вкладці «Пошук», перемикайте вкладки зі saveState і без нього.",
        icon = Icons.Filled.Favorite,
        content = { TopLevelPatternsDemo() },
    ),
    DemoItem(
        route = "process_death",
        title = "Process death і SavedStateHandle",
        summary = "remember vs rememberSaveable vs ViewModel vs SavedStateHandle",
        lecture = Lecture.L7,
        slides = listOf("Слайд 14 · Навігація та process death", "Слайд 17 · SavedStateHandle і аргументи навігації"),
        tryIt = "відкрийте деталі, натисніть усі чотири лічильники, поверніть екран; потім виконайте adb shell am kill.",
        icon = Icons.Filled.Warning,
        content = { ProcessDeathDemo() },
    ),
    DemoItem(
        route = "nav_planning",
        title = "Планування навігації: перевірка графа",
        summary = "Повнота, досяжність, тупики, глибина стеку",
        lecture = Lecture.L7,
        slides = listOf("Слайд 15 · Планування навігації проєкту", "Слайд 16 · Практика: ескіз графа", "Слайд 18 · Приклад: граф для CRUD-застосунку"),
        tryIt = "завантажте «Граф із помилками» і виправте його, поки всі перевірки не стануть зеленими.",
        icon = Icons.Filled.Create,
        content = { NavPlanningDemo() },
    ),

    // ───────────────────────────── Лекція 9 ─────────────────────────────
    DemoItem(
        route = "screen_structure",
        title = "Структура composable-екрана",
        summary = "ListScreen(items, onItemClick, onRefresh) без прив'язки до джерела",
        lecture = Lecture.L9,
        slides = listOf("Слайд 4 · Структурування composable-екранів"),
        tryIt = "перемикайте джерело даних — екран той самий, змінюються лише параметри.",
        icon = Icons.Filled.Settings,
        content = { ScreenStructureDemo() },
    ),
    DemoItem(
        route = "state_hoisting",
        title = "State hoisting",
        summary = "Stateful vs stateless, value + onValueChange",
        lecture = Lecture.L9,
        slides = listOf("Слайд 6 · State Hoisting: основи"),
        tryIt = "натисніть обидва лічильники, поверніть екран: з remember значення зникне, з піднятим rememberSaveable — ні.",
        icon = Icons.Filled.Refresh,
        content = { StateHoistingDemo() },
    ),
    DemoItem(
        route = "reusable_components",
        title = "Перевикористовувані компоненти",
        summary = "AppCard, PrimaryButton, LoadingIndicator та параметризація",
        lecture = Lecture.L9,
        slides = listOf("Слайд 5 · Перевикористовувані компоненти", "Слайд 14 · Типові помилки (дублювання UI-коду)"),
        tryIt = "перетягніть повзунок відступу — зміниться ОДРАЗУ кілька карток; перемикайте параметри PrimaryButton.",
        icon = Icons.Filled.Star,
        content = { ReusableComponentsDemo() },
    ),
    DemoItem(
        route = "scaffold_slots",
        title = "Scaffold, TopAppBar, BottomBar",
        summary = "Слоти, innerPadding, колапсні шапки, FAB + Snackbar",
        lecture = Lecture.L9,
        slides = listOf("Слайд 9 · Scaffold: каркас екрана", "Слайд 10 · TopAppBar та BottomBar"),
        tryIt = "вимкніть «innerPadding» і прокрутіть список; оберіть LargeTopAppBar; натисніть FAB.",
        icon = Icons.Filled.Check,
        content = { ScaffoldSlotsDemo() },
    ),
    DemoItem(
        route = "ui_states",
        title = "UI-стани: Loading / Success / Error / Empty",
        summary = "sealed interface ScreenUiState + when(uiState)",
        lecture = Lecture.L9,
        slides = listOf("Слайд 12 · Стани екрана: Loading / Success / Error", "Слайд 14 · Типові помилки (відсутність Loading/Error)"),
        tryIt = "оберіть «Помилка» і натисніть «Завантажити»; потім «Порожньо» — зверніть увагу, що це різні стани.",
        icon = Icons.Filled.Info,
        content = { UiStatesDemo() },
    ),
    DemoItem(
        route = "error_handling",
        title = "Обробка помилок мережі в UI",
        summary = "Snackbar з «Повторити», повноекранна помилка, inline-помилка, редирект",
        lecture = Lecture.L9,
        slides = listOf("Слайд 17 · Обробка помилок мережі в UI"),
        tryIt = "по черзі натисніть чотири ситуації й порівняйте реакцію UI; у Snackbar натисніть «Повторити».",
        icon = Icons.Filled.Notifications,
        content = { ErrorHandlingDemo() },
    ),
    DemoItem(
        route = "pull_to_refresh",
        title = "Pull-to-refresh",
        summary = "PullToRefreshBox: оновлення поверх наявного контенту",
        lecture = Lecture.L9,
        slides = listOf("Слайд 18 · Pull-to-refresh патерн"),
        tryIt = "потягніть список вниз; увімкніть «Імітувати помилку» — дані лишаться, з'явиться Snackbar.",
        icon = Icons.Filled.Refresh,
        content = { PullToRefreshDemo() },
    ),
    DemoItem(
        route = "adaptive_layouts",
        title = "Адаптивні макети",
        summary = "BoxWithConstraints, breakpoints 600/840 dp, LocalConfiguration",
        lecture = Lecture.L9,
        slides = listOf("Слайд 13 · Адаптивні макети: базово"),
        tryIt = "рухайте повзунок ширини — телефонний макет стає двопанельним; поверніть реальний пристрій.",
        icon = Icons.Filled.DateRange,
        content = { AdaptiveLayoutsDemo() },
    ),
    DemoItem(
        route = "back_handler",
        title = "BackHandler",
        summary = "Перехоплення «Назад» лише за незбережених змін",
        lecture = Lecture.L9,
        slides = listOf("Слайд 8 · Обробка системної кнопки «Назад»"),
        tryIt = "відкрийте форму, наберіть текст і натисніть системну «Назад»; збережіть — і «Назад» знову працює звичайно.",
        icon = Icons.Filled.Lock,
        content = { BackHandlerDemo() },
    ),
    DemoItem(
        route = "shared_viewmodel",
        title = "Передача даних між екранами",
        summary = "Аргументи маршруту vs спільний ViewModel графа vs глобальний стан",
        lecture = Lecture.L9,
        slides = listOf("Слайд 7 · Передача даних між екранами"),
        tryIt = "пройдіть три кроки майстра: номер ViewModel не змінюється; завершіть і почніть знову — номер новий.",
        icon = Icons.Filled.Person,
        content = { SharedViewModelDemo() },
    ),
    DemoItem(
        route = "skeleton_checklist",
        title = "Чек-лист міцного скелета",
        summary = "Типові помилки, чек-лист, практика «скелет навігації»",
        lecture = Lecture.L9,
        slides = listOf("Слайд 14 · Типові помилки", "Слайд 15 · Чек-лист міцного скелета", "Слайд 16 · Практика: скелет навігації проєкту"),
        tryIt = "відмічайте пункти й дивіться, у якому файлі цього застосунку кожен пункт реалізовано.",
        icon = Icons.Filled.Face,
        content = { SkeletonChecklistDemo() },
    ),

    // ───────────────────────────── Підсумок ─────────────────────────────
    DemoItem(
        route = "crud_app",
        title = "Міні-застосунок «Курси» (CRUD)",
        summary = "Усе разом: граф, аргументи, ViewModel, стани, Snackbar, BackHandler",
        lecture = Lecture.Capstone,
        slides = listOf(
            "Л7, слайд 18 · Приклад: граф для CRUD-застосунку",
            "Л9, слайд 11 · Патерн список / деталі",
            "Л9, слайди 4–18 · усе разом",
        ),
        tryIt = "додайте, відредагуйте, видаліть курс; увімкніть помилку мережі й потягніть список; відкрийте неіснуючий курс #999; у формі наберіть текст і натисніть «Назад».",
        icon = Icons.Filled.PlayArrow,
        scrollable = false,
        content = { CrudApp() },
    ),
)
