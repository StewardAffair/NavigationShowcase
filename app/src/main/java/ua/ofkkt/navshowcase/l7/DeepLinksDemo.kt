package ua.ofkkt.navshowcase.l7

import android.content.Intent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import ua.ofkkt.navshowcase.ui.common.ActionRow
import ua.ofkkt.navshowcase.ui.common.BackStackViewer
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.MiniScreen
import ua.ofkkt.navshowcase.ui.common.PrimaryButton
import ua.ofkkt.navshowcase.ui.common.SandboxFrame
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/** Схема deep link, зареєстрована в AndroidManifest і в кореневому графі (app/AppRoot.kt). */
const val REAL_DEEP_LINK_PATTERN = "navshowcase://demo/item/{id}"

/**
 * Лекція 7 · слайд «Deep links: огляд» — navDeepLink { uriPattern = "..." } усередині composable().
 */
@Composable
fun DeepLinksDemo() {
    val context = LocalContext.current
    val nav = rememberNavController()
    var uri by rememberSaveable { mutableStateOf("myapp://product/7") }
    var result by rememberSaveable { mutableStateOf("—") }

    DemoSection(
        "1. Deep link усередині графа",
        note = "Destination product/{id} оголошує uriPattern. NavController уміє перейти за URI — так само, як це робить система з push-сповіщення чи посилання.",
    ) {
        SandboxFrame(height = 170.dp) {
            NavHost(navController = nav, startDestination = "home") {
                composable("home") {
                    MiniScreen(title = "Головна", subtitle = "route = \"home\"")
                }
                composable(
                    route = "product/{id}",
                    arguments = listOf(navArgument("id") { type = NavType.IntType }),
                    deepLinks = listOf(navDeepLink { uriPattern = "myapp://product/{id}" }),
                ) { entry ->
                    MiniScreen(
                        title = "Товар #${entry.arguments?.getInt("id")}",
                        subtitle = "deepLink: myapp://product/{id}",
                    )
                }
            }
        }
        BackStackViewer(nav)
        OutlinedTextField(
            value = uri,
            onValueChange = { uri = it },
            label = { Text("URI") },
            singleLine = true,
        )
        ActionRow(
            listOf(
                "myapp://product/7" to { uri = "myapp://product/7" },
                "myapp://product/abc" to { uri = "myapp://product/abc" },
                "myapp://other/1" to { uri = "myapp://other/1" },
            ),
        )
        PrimaryButton(
            text = "navigate(uri.toUri())",
            onClick = {
                result = try {
                    nav.navigate(uri.trim().toUri())
                    "✅ Збіг: екран відкрито"
                } catch (e: IllegalArgumentException) {
                    "❌ Жоден destination не оголошує такий deep link"
                }
            },
        )
        Text("Результат: $result", style = MaterialTheme.typography.bodyMedium)
    }

    DemoSection(
        "2. Справжній deep link із системи",
        note = "Кнопка надсилає ACTION_VIEW з URI цього застосунку. Система повертає його в MainActivity (singleTop), а NavController відкриває потрібний екран.",
    ) {
        Text("Схема: $REAL_DEEP_LINK_PATTERN", style = MaterialTheme.typography.bodyMedium)
        ActionRow(
            listOf(7, 42).map { id ->
                "Відкрити navshowcase://demo/item/$id" to {
                    val intent = Intent(Intent.ACTION_VIEW, "navshowcase://demo/item/$id".toUri())
                        .setPackage(context.packageName)
                    context.startActivity(intent)
                }
            },
        )
        Tip(
            "Якщо застосунок було закрито, NavController сам побудує back stack (каталог → екран), і «Назад» поверне до каталогу. " +
                "Якщо він уже відкритий — екран додається поверх поточного.",
            TipKind.Success,
        )
        Text("Ту саму перевірку можна зробити з комп'ютера (застосунок має бути встановлений):", style = MaterialTheme.typography.bodySmall)
        CodeSnippet(
            """
            adb shell am start -a android.intent.action.VIEW \
                -d "navshowcase://demo/item/42" ua.ofkkt.navshowcase
            """,
        )
    }

    DemoSection("3. Що потрібно для deep link") {
        CodeSnippet(
            """
            // 1) у графі навігації
            composable(
                route = "item/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType }),
                deepLinks = listOf(navDeepLink { uriPattern = "navshowcase://demo/item/{id}" }),
            ) { ... }

            // 2) у AndroidManifest.xml — intent-filter для MainActivity
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="navshowcase" android:host="demo" />
            </intent-filter>

            // 3) якщо Activity уже запущено (launchMode=singleTop) — передати новий Intent у NavController
            addOnNewIntentListener { navController.handleDeepLink(it) }
            """,
        )
        Tip(
            "Deep links критичні для push-сповіщень: клік має вести користувача прямо на потрібний екран. " +
                "Детальна реалізація (App Links, перевірка домену) виходить за межі базового курсу.",
            TipKind.Info,
        )
    }
}
