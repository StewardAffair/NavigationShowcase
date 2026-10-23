package ua.ofkkt.navshowcase.l7

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ua.ofkkt.navshowcase.ui.common.ActionRow
import ua.ofkkt.navshowcase.ui.common.BackStackViewer
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.MiniScreen
import ua.ofkkt.navshowcase.ui.common.SandboxFrame
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 7 · слайд «Back stack: як це працює» — LIFO, navigate/popBackStack,
 * popUpTo(inclusive = false/true), launchSingleTop.
 */
@Composable
fun BackStackDemo() {
    val nav = rememberNavController()

    DemoSection(
        "1. Стек у реальному часі",
        note = "Кнопки екранів додають записи. Дивіться, як змінюється стек під рамкою. Системна «Назад» теж знімає верхній запис.",
    ) {
        SandboxFrame(height = 190.dp) {
            NavHost(navController = nav, startDestination = "a") {
                composable("a") { LetterScreen("A", "b", "B", nav) }
                composable("b") { LetterScreen("B", "c", "C", nav) }
                composable("c") { LetterScreen("C", "d", "D", nav) }
                composable("d") {
                    MiniScreen(
                        title = "Екран D",
                        subtitle = "route = \"d\"",
                        actions = listOf("← popBackStack()" to { nav.popBackStack() }),
                    )
                }
            }
        }
        BackStackViewer(nav)
    }

    DemoSection(
        "2. Керуйте тим, що лишається у стеку",
        note = "Спробуйте комбінації: спершу пройдіть A → B → C → D, потім натискайте кнопки.",
    ) {
        Text("popUpTo — очищення стеку під час переходу", style = MaterialTheme.typography.labelLarge)
        ActionRow(
            listOf(
                "navigate(a) + popUpTo(b) inclusive=false" to {
                    nav.navigate("a") { popUpTo("b") { inclusive = false } }
                },
                "navigate(a) + popUpTo(b) inclusive=true" to {
                    nav.navigate("a") { popUpTo("b") { inclusive = true } }
                },
                "Очистити весь стек → A (login → home)" to {
                    nav.navigate("a") { popUpTo(nav.graph.id) { inclusive = true } }
                },
            ),
        )
        Text("launchSingleTop — без дублікатів", style = MaterialTheme.typography.labelLarge)
        ActionRow(
            listOf(
                "navigate(c)" to { nav.navigate("c") },
                "navigate(c) { launchSingleTop }" to { nav.navigate("c") { launchSingleTop = true } },
            ),
        )
        Text("popBackStack — повернення", style = MaterialTheme.typography.labelLarge)
        ActionRow(
            listOf(
                "popBackStack()" to { if (nav.previousBackStackEntry != null) nav.popBackStack() else false },
                "popBackStack(b, inclusive=false)" to { nav.popBackStack("b", inclusive = false) },
                "popBackStack(b, inclusive=true)" to { nav.popBackStack("b", inclusive = true) },
            ),
        )
        Tip(
            "Якщо стек став порожнім — натисніть «Очистити весь стек → A», щоб відновити пісочницю.",
            TipKind.Info,
        )
    }

    DemoSection("3. Шпаргалка") {
        CodeSnippet(
            """
            // очистити стек до певного екрана (сам екран лишається)
            navigate("home") { popUpTo("list") { inclusive = false } }

            // очистити ВЕСЬ стек (login → home)
            navigate("home") { popUpTo(navController.graph.id) { inclusive = true } }

            // inclusive = true видаляє зі стеку й сам екран, до якого зроблено popUpTo
            // launchSingleTop = true — не створювати дублікат, якщо цей екран уже зверху
            """,
        )
        Tip("Back stack — «останній прийшов, перший вийшов» (LIFO). Кожен navigate() додає екран на верхівку, popBackStack() знімає верхній.", TipKind.Success)
    }
}

@Composable
private fun LetterScreen(
    letter: String,
    nextRoute: String,
    nextLetter: String,
    nav: androidx.navigation.NavController,
) {
    MiniScreen(
        title = "Екран $letter",
        subtitle = "route = \"${letter.lowercase()}\"",
        actions = buildList<Pair<String, () -> Any?>> {
            add("navigate(\"$nextRoute\") → $nextLetter" to { nav.navigate(nextRoute) })
            if (nav.previousBackStackEntry != null) {
                add("← popBackStack()" to { nav.popBackStack() })
            }
        },
    )
}
