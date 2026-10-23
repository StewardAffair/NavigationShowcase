@file:OptIn(ExperimentalMaterial3Api::class)

package ua.ofkkt.navshowcase.l7

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import ua.ofkkt.navshowcase.ui.common.ChoiceChips
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.CompareTable
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.LabeledSwitch
import ua.ofkkt.navshowcase.ui.common.MiniScreen
import ua.ofkkt.navshowcase.ui.common.NoInsets
import ua.ofkkt.navshowcase.ui.common.SandboxFrame
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 7 · слайд «Патерни навігації: Bottom Nav, Drawer»;
 * Лекція 9 · слайд «TopAppBar та BottomBar» (BottomBar — на рівні кореневого NavHost і зникає в «глибоких» підекранах).
 */
private enum class Pattern(val label: String) { Bottom("Bottom Navigation"), Drawer("Navigation Drawer") }

@Composable
fun TopLevelPatternsDemo() {
    var pattern by rememberSaveable { mutableStateOf(Pattern.Bottom) }
    var keepState by rememberSaveable { mutableStateOf(true) }

    DemoSection(
        "1. Оберіть патерн верхнього рівня",
        note = "Вибір залежить від кількості основних розділів і цільової аудиторії.",
    ) {
        ChoiceChips(Pattern.entries, pattern, { pattern = it }, { it.label })
        if (pattern == Pattern.Bottom) {
            LabeledSwitch("saveState / restoreState для вкладок", keepState, { keepState = it })
            SandboxFrame(height = 380.dp) { BottomNavSandbox(keepState) }
            Tip(
                "Лічильник на вкладці «Пошук» живе в rememberSaveable. Натисніть його кілька разів, перейдіть на іншу вкладку і поверніться: " +
                    "зі saveState/restoreState він збережеться, без них — обнулиться. На екрані «Деталі» нижня панель зникає: " +
                    "BottomBar живе на рівні кореневого графа і показується лише на кореневих екранах.",
                TipKind.Info,
            )
        } else {
            SandboxFrame(height = 380.dp) { DrawerSandbox() }
            Tip("Drawer підходить, коли розділів більше 5 або список динамічний. Він потребує явної дії користувача (кнопка ☰ або свайп від краю).", TipKind.Info)
        }
    }

    DemoSection("2. Порівняння") {
        CompareTable(
            headers = listOf("Патерн", "Кількість розділів", "Доступність", "Найкраще для"),
            rows = listOf(
                listOf("Bottom Navigation", "3–5", "Завжди видима", "Основні мобільні застосунки"),
                listOf("Navigation Drawer", "понад 5", "По жесту або кнопці", "Складні застосунки, планшети"),
            ),
        )
    }

    DemoSection("3. Ключовий код") {
        CodeSnippet(
            """
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination

            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(tab.label) },
            )
            """,
        )
    }
}

private class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
private fun BottomNavSandbox(keepState: Boolean) {
    val nav = rememberNavController()
    val tabs = listOf(
        Tab("home", "Головна", Icons.Filled.Home),
        Tab("search", "Пошук", Icons.Filled.Search),
        Tab("profile", "Профіль", Icons.Filled.Person),
    )
    val backEntry by nav.currentBackStackEntryAsState()
    val destination = backEntry?.destination
    // Панель видима лише на кореневих екранах-вкладках
    val showBar = tabs.any { it.route == destination?.route }

    Scaffold(
        contentWindowInsets = NoInsets,
        bottomBar = {
            if (showBar) {
                NavigationBar(windowInsets = NoInsets) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = destination?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = keepState }
                                    launchSingleTop = true
                                    restoreState = keepState
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { inner ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(inner)) {
            composable("home") {
                MiniScreen(
                    title = "Головна",
                    actions = listOf("Відкрити деталі (без панелі)" to { nav.navigate("detail/1") }),
                )
            }
            composable("search") {
                var taps by rememberSaveable { mutableStateOf(0) }
                MiniScreen(
                    title = "Пошук",
                    actions = listOf("Натиснуто: $taps" to { taps++ }),
                )
            }
            composable("profile") { MiniScreen(title = "Профіль") }
            composable("detail/{id}") { entry ->
                MiniScreen(
                    title = "Деталі #${entry.arguments?.getString("id")}",
                    subtitle = "підекран: BottomBar прихований",
                    actions = listOf("← Назад" to { nav.popBackStack() }),
                )
            }
        }
    }
}

private class DrawerItem(val route: String, val title: String, val icon: ImageVector)

@Composable
private fun DrawerSandbox() {
    val nav = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val items = listOf(
        DrawerItem("inbox", "Вхідні", Icons.Filled.Email),
        DrawerItem("starred", "Позначені", Icons.Filled.Star),
        DrawerItem("shared", "Спільні", Icons.Filled.Share),
        DrawerItem("drafts", "Чернетки", Icons.Filled.Create),
        DrawerItem("trash", "Кошик", Icons.Filled.Delete),
        DrawerItem("settings", "Налаштування", Icons.Filled.Settings),
        DrawerItem("about", "Про застосунок", Icons.Filled.Info),
    )
    val backEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backEntry?.destination?.route
    val title = items.firstOrNull { it.route == currentRoute }?.title ?: ""

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(windowInsets = NoInsets) {
                items.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.title) },
                        icon = { Icon(item.icon, contentDescription = null) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            nav.navigate(item.route) {
                                popUpTo(nav.graph.findStartDestination().id)
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }
        },
    ) {
        Scaffold(
            contentWindowInsets = NoInsets,
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    windowInsets = NoInsets,
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Відкрити меню")
                        }
                    },
                )
            },
        ) { inner ->
            NavHost(nav, startDestination = "inbox", modifier = Modifier.padding(inner)) {
                items.forEach { item ->
                    composable(item.route) {
                        MiniScreen(title = item.title, subtitle = "route = \"${item.route}\"")
                    }
                }
            }
        }
    }
}
