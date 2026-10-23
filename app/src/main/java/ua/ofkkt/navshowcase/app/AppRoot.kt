@file:OptIn(ExperimentalMaterial3Api::class)

package ua.ofkkt.navshowcase.app

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.util.Consumer
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import ua.ofkkt.navshowcase.l7.REAL_DEEP_LINK_PATTERN
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.PrimaryButton

/**
 * Кореневий граф застосунку (Лекція 7: Single-Activity; Лекція 9: «Повний навігаційний граф»).
 * Кожен екран-демо — один composable-destination з унікальним маршрутом.
 */
@Composable
fun AppRoot() {
    val navController = rememberNavController()
    HandleNewIntents(navController)

    NavHost(navController = navController, startDestination = Routes.HUB) {
        composable(Routes.HUB) {
            HubScreen(onOpen = { route -> navController.navigate(route) })
        }

        Demos.forEach { demo ->
            composable(demo.route) {
                DemoHost(demo = demo, onBack = { navController.popBackStack() })
            }
        }

        // Реальний deep link: navshowcase://demo/item/{id} (також оголошений в AndroidManifest)
        composable(
            route = Routes.DEEP_TARGET,
            arguments = listOf(navArgument("id") { type = NavType.IntType }),
            deepLinks = listOf(navDeepLink { uriPattern = REAL_DEEP_LINK_PATTERN }),
        ) { entry ->
            DeepLinkTargetScreen(
                id = entry.arguments?.getInt("id") ?: -1,
                onBack = { navController.popBackStack() },
                onHome = {
                    navController.navigate(Routes.HUB) { popUpTo(Routes.HUB) { inclusive = true } }
                },
            )
        }
    }
}

/**
 * Activity з launchMode=singleTop отримує повторні Intent через onNewIntent.
 * Передаємо їх у NavController, щоб deep link відкрив потрібний екран (Лекція 7: «Deep links»).
 */
@Composable
private fun HandleNewIntents(navController: NavController) {
    val activity = LocalContext.current.findComponentActivity()
    DisposableEffect(activity, navController) {
        val listener = Consumer<Intent> { intent -> navController.handleDeepLink(intent) }
        activity?.addOnNewIntentListener(listener)
        onDispose { activity?.removeOnNewIntentListener(listener) }
    }
}

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}

/**
 * Оболонка демо-екрана: Scaffold з TopAppBar («Назад», назва, лекція) + картка «що демонструє цей екран».
 * Для екранів без власного прокручування (scrollable = false) пояснення відкривається кнопкою ⓘ.
 */
@Composable
private fun DemoHost(demo: DemoItem, onBack: () -> Unit) {
    var showInfo by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(demo.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                        Text(demo.lecture.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (!demo.scrollable) {
                        IconButton(onClick = { showInfo = true }) {
                            Icon(Icons.Outlined.Info, contentDescription = "Про цей екран")
                        }
                    }
                },
            )
        },
    ) { inner ->
        if (demo.scrollable) {
            Column(
                Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                LectureCard(demo)
                demo.content()
            }
        } else {
            val shape = MaterialTheme.shapes.medium
            Box(
                Modifier
                    .padding(inner)
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(shape)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, shape),
            ) {
                demo.content()
            }
        }
    }

    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            confirmButton = { TextButton(onClick = { showInfo = false }) { Text("Зрозуміло") } },
            title = { Text(demo.title) },
            text = { LectureInfo(demo) },
        )
    }
}

@Composable
private fun LectureCard(demo: DemoItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Box(Modifier.padding(16.dp)) { LectureInfo(demo) }
    }
}

@Composable
private fun LectureInfo(demo: DemoItem) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("${demo.lecture.label} · ${demo.lecture.title}", style = MaterialTheme.typography.labelLarge)
        demo.slides.forEach { Text("📑 $it", style = MaterialTheme.typography.bodySmall) }
        Text("Спробуйте: ${demo.tryIt}", style = MaterialTheme.typography.bodyMedium)
    }
}

/** Екран, на який веде справжній deep link navshowcase://demo/item/{id}. */
@Composable
private fun DeepLinkTargetScreen(id: Int, onBack: () -> Unit, onHome: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deep link → item #$id") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier.padding(inner).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Ви відкрили цей екран напряму за посиланням, а не проходячи каталог. " +
                    "Аргумент id = $id NavController витягнув із URI за шаблоном $REAL_DEEP_LINK_PATTERN.",
                style = MaterialTheme.typography.bodyLarge,
            )
            CodeSnippet("navshowcase://demo/item/$id")
            Text(
                "Якщо застосунок було закрито, NavController сам побудує back stack: каталог → цей екран. " +
                    "Якщо він уже був відкритий, екран додається поверх поточного.",
                style = MaterialTheme.typography.bodyMedium,
            )
            PrimaryButton("На головну", onHome)
        }
    }
}
