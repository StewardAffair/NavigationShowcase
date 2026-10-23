package ua.ofkkt.navshowcase.l7

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ua.ofkkt.navshowcase.ui.common.ActionRow
import ua.ofkkt.navshowcase.ui.common.BackStackViewer
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.MiniScreen
import ua.ofkkt.navshowcase.ui.common.SandboxFrame
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind
import androidx.compose.ui.unit.dp

/**
 * Лекція 7 · слайди «Навігаційний граф: концепція», «NavHost та NavController»,
 * «composable() destinations». Route-константи — через sealed class (порада зі слайда).
 */
sealed class SandboxRoute(val route: String) {
    data object Items : SandboxRoute("list")
    data object Detail : SandboxRoute("detail/{id}") {
        fun create(id: Int) = "detail/$id"
    }
    data object Edit : SandboxRoute("edit/{id}") {
        fun create(id: Int) = "edit/$id"
    }
}

@Composable
fun NavBasicsDemo() {
    val nav = rememberNavController()
    val currentEntry by nav.currentBackStackEntryAsState()

    DemoSection(
        "1. Граф у дії: Список → Деталі → Редагувати",
        note = "Рамка нижче — справжній NavHost із власним NavController. Кожен екран — composable-destination з унікальним route.",
    ) {
        SandboxFrame(height = 230.dp) {
            NavHost(navController = nav, startDestination = SandboxRoute.Items.route) {
                composable(SandboxRoute.Items.route) {
                    MiniScreen(
                        title = "Список",
                        subtitle = "route = \"list\"",
                        actions = listOf(
                            "Відкрити деталі #1" to { nav.navigate(SandboxRoute.Detail.create(1)) },
                            "Відкрити деталі #2" to { nav.navigate(SandboxRoute.Detail.create(2)) },
                        ),
                    )
                }
                composable(SandboxRoute.Detail.route) { entry ->
                    val id = entry.arguments?.getString("id")
                    MiniScreen(
                        title = "Деталі #$id",
                        subtitle = "route = \"detail/{id}\"",
                        actions = listOf(
                            "Редагувати" to { nav.navigate(SandboxRoute.Edit.create(id?.toIntOrNull() ?: 0)) },
                            "← popBackStack()" to { nav.popBackStack() },
                        ),
                    )
                }
                composable(SandboxRoute.Edit.route) { entry ->
                    val id = entry.arguments?.getString("id")
                    MiniScreen(
                        title = "Редагування #$id",
                        subtitle = "route = \"edit/{id}\"",
                        actions = listOf("← popBackStack()" to { nav.popBackStack() }),
                    )
                }
            }
        }
        BackStackViewer(nav)
        Text(
            "NavController каже, що активно зараз: ${currentEntry?.destination?.route}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Tip("Системна кнопка «Назад» теж працює через back stack: натисніть її, поки сфокусовано пісочницю.", TipKind.Info)
    }

    DemoSection(
        "2. NavController — єдине джерело правди",
        note = "Керувати навігацією можна з будь-якого місця, де є navController — навіть ззовні екрана. Екрани не зберігають «поточний екран» у власних змінних.",
    ) {
        ActionRow(
            listOf(
                "navigate(\"detail/1\")" to { nav.navigate(SandboxRoute.Detail.create(1)) },
                "navigate(\"edit/7\")" to { nav.navigate(SandboxRoute.Edit.create(7)) },
                "popBackStack()" to {
                    // popBackStack() на ОСТАННЬОМУ записі лишає порожній NavHost — тому перевіряємо previousBackStackEntry.
                    if (nav.previousBackStackEntry != null) nav.popBackStack() else false
                },
            ),
        )
        Tip(
            "popBackStack() на останньому екрані видаляє і його — NavHost стає порожнім. " +
                "Перевіряйте nav.previousBackStackEntry != null (так зроблено у кнопці вище).",
            TipKind.Warning,
        )
    }

    DemoSection("3. Як це описується в коді") {
        CodeSnippet(
            """
            val navController = rememberNavController()      // стек викликів, єдине джерело правди

            NavHost(navController, startDestination = "list") {   // «вікно» у поточний destination
                composable("list") {
                    ListScreen(onItemClick = { id -> navController.navigate("detail/${'$'}id") })
                }
                composable("detail/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")
                    DetailScreen(id = id)
                }
            }

            navController.navigate("detail/1")   // додає екран у стек
            navController.popBackStack()         // знімає верхній екран
            """,
        )
        Text("Route-константи виносять у sealed class / object, щоб уникнути друкарських помилок у рядках:", style = MaterialTheme.typography.bodyMedium)
        CodeSnippet(
            """
            sealed class Route(val route: String) {
                data object Items : Route("list")
                data object Detail : Route("detail/{id}") {
                    fun create(id: Int) = "detail/${'$'}id"
                }
            }
            """,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Правила з лекції:", style = MaterialTheme.typography.labelLarge)
            Text("• Route — рядок-ідентифікатор екрана, часто з параметрами у фігурних дужках.", style = MaterialTheme.typography.bodySmall)
            Text("• Кожен destination реєструється один раз у графі NavHost.", style = MaterialTheme.typography.bodySmall)
            Text("• Route унікальний у межах одного графа.", style = MaterialTheme.typography.bodySmall)
            Text("• Параметри отримують через backStackEntry.arguments.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
