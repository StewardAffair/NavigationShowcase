package ua.ofkkt.navshowcase.l7

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import ua.ofkkt.navshowcase.ui.common.BackStackViewer
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.CompareTable
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.LabeledSwitch
import ua.ofkkt.navshowcase.ui.common.MiniScreen
import ua.ofkkt.navshowcase.ui.common.SandboxFrame
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 7 · слайд «Вкладені графи (nested graphs)»: auth-граф і main-граф,
 * власний стартовий destination у кожного підграфа, очищення auth-графа після входу.
 */
@Composable
fun NestedGraphsDemo() {
    val nav = rememberNavController()
    var showGraphs by rememberSaveable { mutableStateOf(true) }

    DemoSection(
        "1. Два підграфи: auth і main",
        note = "Натисніть «Увійти» — auth-граф повністю видаляється зі стеку (popUpTo + inclusive), «Назад» уже не поверне на логін.",
    ) {
        SandboxFrame(height = 210.dp) {
            NavHost(navController = nav, startDestination = "auth") {
                navigation(startDestination = "login", route = "auth") {
                    composable("login") {
                        MiniScreen(
                            title = "Вхід",
                            subtitle = "auth → login",
                            actions = listOf(
                                "Увійти" to {
                                    nav.navigate("main") { popUpTo("auth") { inclusive = true } }
                                },
                                "Реєстрація" to { nav.navigate("register") },
                            ),
                        )
                    }
                    composable("register") {
                        MiniScreen(
                            title = "Реєстрація",
                            subtitle = "auth → register",
                            actions = listOf("← Назад" to { nav.popBackStack() }),
                        )
                    }
                }
                navigation(startDestination = "home", route = "main") {
                    composable("home") {
                        MiniScreen(
                            title = "Головна",
                            subtitle = "main → home",
                            actions = listOf(
                                "Деталі #1" to { nav.navigate("detail/1") },
                                "Вийти" to {
                                    nav.navigate("auth") { popUpTo("main") { inclusive = true } }
                                },
                            ),
                        )
                    }
                    composable("detail/{id}") { entry ->
                        MiniScreen(
                            title = "Деталі #${entry.arguments?.getString("id")}",
                            subtitle = "main → detail/{id}",
                            actions = listOf("← Назад" to { nav.popBackStack() }),
                        )
                    }
                }
            }
        }
        LabeledSwitch("Показувати записи графів у стеку", showGraphs, { showGraphs = it })
        BackStackViewer(nav, showGraphs = showGraphs)
    }

    DemoSection("2. Код") {
        CodeSnippet(
            """
            NavHost(navController, startDestination = "auth") {
                navigation(startDestination = "login", route = "auth") {
                    composable("login") { LoginScreen() }
                    composable("register") { RegisterScreen() }
                }
                navigation(startDestination = "home", route = "main") {
                    composable("home") { HomeScreen() }
                    composable("detail/{id}") { ... }
                }
            }

            // після успішного входу — прибрати auth-граф зі стеку
            navController.navigate("main") { popUpTo("auth") { inclusive = true } }
            """,
        )
    }

    DemoSection("3. Типові підграфи і коли їх застосовувати") {
        CompareTable(
            headers = listOf("Підграф", "Екрани"),
            rows = listOf(
                listOf("Онбординг", "вітальний екран, реєстрація, вибір налаштувань"),
                listOf("Авторизація", "логін, реєстрація, відновлення паролю"),
                listOf("Основний", "усі екрани після входу в застосунок"),
                listOf("Налаштування", "профіль, сповіщення, тема"),
            ),
        )
        Tip("Застосовуйте, коли екранів більше 10, є виразні логічні групи або модульна архітектура. Кожен підграф має власний стартовий destination.", TipKind.Info)
        Tip("Nested graphs — спосіб уникнути «одного величезного файлу навігації». Починайте з простого графа, додавайте вкладені — лише коли граф стає складним.", TipKind.Success)
    }
}
