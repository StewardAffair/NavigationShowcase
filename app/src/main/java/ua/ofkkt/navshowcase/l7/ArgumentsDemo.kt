package ua.ofkkt.navshowcase.l7

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ua.ofkkt.navshowcase.ui.common.ActionRow
import ua.ofkkt.navshowcase.ui.common.BackStackViewer
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.MiniScreen
import ua.ofkkt.navshowcase.ui.common.SandboxFrame
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 7 · слайд «Передача аргументів між екранами»:
 * 1) визначення у графі, 2) передача значення, 3) отримання на екрані; типізовані аргументи (NavType),
 * query-параметри з defaultValue, «передавайте ID, а не весь об'єкт».
 */
@Composable
fun ArgumentsDemo() {
    val nav = rememberNavController()
    var query by rememberSaveable { mutableStateOf("compose") }
    var limit by rememberSaveable { mutableStateOf("5") }
    var lastError by rememberSaveable { mutableStateOf<String?>(null) }

    val searchRoute = "search?query=${Uri.encode(query)}&limit=${limit.ifBlank { "10" }}"

    DemoSection(
        "1. Три кроки передачі аргументів",
        note = "Крок 1 — шаблон у графі, крок 2 — підстановка значення в navigate(), крок 3 — читання з backStackEntry.",
    ) {
        SandboxFrame(height = 220.dp) {
            NavHost(navController = nav, startDestination = "users") {
                composable("users") {
                    MiniScreen(
                        title = "Користувачі",
                        subtitle = "route = \"users\"",
                        actions = listOf(
                            "user/1 (Олена)" to { nav.navigate("user/1") },
                            "user/2 (Максим)" to { nav.navigate("user/2") },
                        ),
                    )
                }
                composable(
                    route = "user/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.IntType }),
                ) { entry ->
                    val userId = entry.arguments?.getInt("userId")
                    MiniScreen(
                        title = "Користувач #$userId",
                        subtitle = "userId: Int (NavType.IntType)",
                    )
                }
                composable(
                    route = "search?query={query}&limit={limit}",
                    arguments = listOf(
                        navArgument("query") { type = NavType.StringType; defaultValue = "" },
                        navArgument("limit") { type = NavType.IntType; defaultValue = 10 },
                    ),
                ) { entry -> SearchArgsScreen(entry) }
            }
        }
        BackStackViewer(nav)
        CodeSnippet(
            """
            // 1. Визначення у графі
            composable(
                route = "user/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.IntType }),
            ) { backStackEntry ->
                // 3. Отримання на екрані
                val userId = backStackEntry.arguments?.getInt("userId")
                UserScreen(userId)
            }

            // 2. Передача значення
            navController.navigate("user/${'$'}id")
            """,
        )
    }

    DemoSection(
        "2. Опційні параметри (query) і defaultValue",
        note = "Для необов'язкових параметрів використовують query-синтаксис: screen?param={param}. Завжди вказуйте defaultValue.",
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("query") },
            singleLine = true,
        )
        OutlinedTextField(
            value = limit,
            onValueChange = { limit = it.filter { c -> c.isDigit() }.take(3) },
            label = { Text("limit (Int)") },
            singleLine = true,
        )
        Text("navigate(\"$searchRoute\")", style = MaterialTheme.typography.bodySmall)
        ActionRow(
            listOf(
                "З параметрами" to { nav.navigate(searchRoute) },
                "Без параметрів (defaults)" to { nav.navigate("search") },
            ),
        )
        Tip("Рядок у query-параметрі кодуйте через Uri.encode() — інакше пробіли та & зламають маршрут.", TipKind.Warning)
    }

    DemoSection(
        "3. Типобезпека: що буде з user/abc?",
        note = "userId оголошено як Int. Рядок, що не парситься в число, не збігається з жодним destination.",
    ) {
        ActionRow(
            listOf(
                "navigate(\"user/abc\")" to {
                    lastError = try {
                        nav.navigate("user/abc")
                        null
                    } catch (e: IllegalArgumentException) {
                        "IllegalArgumentException: destination не знайдено"
                    }
                },
                "navigate(\"user/7\")" to {
                    lastError = null
                    nav.navigate("user/7")
                },
            ),
        )
        if (lastError != null) {
            Tip(lastError.orEmpty(), TipKind.Warning)
        }
    }

    DemoSection("4. Що передавати") {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("✅ Передавайте ID, а не весь об'єкт — деталі довантажуйте на екрані призначення через ViewModel.", style = MaterialTheme.typography.bodyMedium)
            Text("✅ Для необов'язкових значень — query-параметри з defaultValue (null для «нового запису» → -1 або спеціальне значення).", style = MaterialTheme.typography.bodyMedium)
            Text("⚠️ Уникайте серіалізації складних об'єктів у route: великі дані можуть спричинити TransactionTooLargeException, а після process death об'єкт може не відновитися.", style = MaterialTheme.typography.bodyMedium)
        }
        Tip("Аргументи простих типів (String, Int, Boolean) Navigation Compose зберігає і відновлює автоматично.", TipKind.Success)
    }
}

@Composable
private fun SearchArgsScreen(entry: NavBackStackEntry) {
    val query = entry.arguments?.getString("query").orEmpty()
    val limit = entry.arguments?.getInt("limit")
    MiniScreen(
        title = "Пошук",
        subtitle = "search?query={query}&limit={limit}",
    ) {
        Text("query = \"$query\"", style = MaterialTheme.typography.bodyMedium)
        Text("limit = $limit", style = MaterialTheme.typography.bodyMedium)
    }
}
