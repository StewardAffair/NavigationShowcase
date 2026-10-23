package ua.ofkkt.navshowcase.l9

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import ua.ofkkt.navshowcase.ui.common.BackStackViewer
import ua.ofkkt.navshowcase.ui.common.ChoiceChips
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.CompareTable
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.MiniScreen
import ua.ofkkt.navshowcase.ui.common.SandboxFrame
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 9 · слайд «Передача даних між екранами»: аргументи маршруту · спільний ViewModel (graph-scoped) ·
 * чого уникати: глобальний стан. Тут — реєстрація на курс у три кроки; усі кроки ділять ОДИН ViewModel,
 * прив'язаний до навігаційного графа "wizard", а не до окремого екрана.
 */
class WizardViewModel : ViewModel() {
    var name by mutableStateOf("")
    var course by mutableStateOf<String?>(null)
    val instanceId: Int = System.identityHashCode(this)
}

private const val WIZARD_GRAPH = "wizard"

@Composable
private fun wizardViewModel(nav: NavController, entry: NavBackStackEntry): WizardViewModel {
    // Власник ViewModel — запис графа "wizard", а не поточного екрана
    val parentEntry = remember(entry) { nav.getBackStackEntry(WIZARD_GRAPH) }
    return viewModel<WizardViewModel>(parentEntry)
}

@Composable
fun SharedViewModelDemo() {
    val nav = rememberNavController()

    DemoSection(
        "1. Реєстрація на курс: три кроки — один ViewModel",
        note = "Введіть ім'я, оберіть курс — обидва значення зберігаються у спільному WizardViewModel. Номер екземпляра однаковий на всіх кроках. Вийдіть із майстра — ViewModel знищується.",
    ) {
        SandboxFrame(height = 300.dp) {
            NavHost(navController = nav, startDestination = "intro") {
                composable("intro") {
                    MiniScreen(
                        title = "Реєстрація на курс",
                        actions = listOf("Почати" to { nav.navigate(WIZARD_GRAPH) }),
                    )
                }
                navigation(startDestination = "step1", route = WIZARD_GRAPH) {
                    composable("step1") { entry ->
                        val vm = wizardViewModel(nav, entry)
                        MiniScreen(
                            title = "Крок 1 з 3: ім'я",
                            subtitle = "WizardViewModel #${vm.instanceId}",
                            actions = listOf(
                                "Далі →" to { nav.navigate("step2") },
                                "Скасувати" to { nav.popBackStack("intro", inclusive = false) },
                            ),
                        ) {
                            OutlinedTextField(
                                value = vm.name,
                                onValueChange = { vm.name = it },
                                label = { Text("Ім'я") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            )
                        }
                    }
                    composable("step2") { entry ->
                        val vm = wizardViewModel(nav, entry)
                        val courses = listOf("Kotlin", "Compose", "KMP")
                        MiniScreen(
                            title = "Крок 2 з 3: курс",
                            subtitle = "WizardViewModel #${vm.instanceId}",
                            actions = listOf(
                                "Далі →" to { nav.navigate("step3") },
                                "← Назад" to { nav.popBackStack() },
                            ),
                        ) {
                            ChoiceChips(courses, vm.course ?: "", { vm.course = it }, { it })
                        }
                    }
                    composable("step3") { entry ->
                        val vm = wizardViewModel(nav, entry)
                        MiniScreen(
                            title = "Крок 3 з 3: підсумок",
                            subtitle = "WizardViewModel #${vm.instanceId}",
                            actions = listOf(
                                "Завершити" to { nav.popBackStack("intro", inclusive = false) },
                                "← Назад" to { nav.popBackStack() },
                            ),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Ім'я: ${vm.name.ifBlank { "—" }}")
                                Text("Курс: ${vm.course ?: "—"}")
                            }
                        }
                    }
                }
            }
        }
        BackStackViewer(nav, showGraphs = true)
        OutlinedButton(onClick = { if (nav.previousBackStackEntry != null) nav.popBackStack() else false }) {
            Text("popBackStack()")
        }
        Tip("Після «Завершити» або «Скасувати» граф wizard залишає back stack і його ViewModel знищується: наступне «Почати» створить новий екземпляр (інший номер).", TipKind.Info)
    }

    DemoSection("2. Три способи передати дані між екранами") {
        CompareTable(
            headers = listOf("Спосіб", "Для чого", "Примітка"),
            rows = listOf(
                listOf("Аргументи маршруту", "Прості ідентифікатори: itemId, userId", "Найпростіший і найпрозоріший — за замовчуванням для ID"),
                listOf("Спільний ViewModel (graph-scoped)", "Складніші дані, що не серіалізуються в рядок", "ViewModel прив'язаний до графа, а не до екрана"),
                listOf("⚠️ Глобальний стан", "— (уникати)", "Глобальні змінні, object-синглтони, статичні поля"),
            ),
        )
        Tip("Глобальний mutable стан — джерело важковідстежуваних багів: код стає непередбачуваним, ускладнюється тестування. Кожен екран має явно декларувати, які дані йому потрібні.", TipKind.Warning)
    }

    DemoSection("3. Код") {
        CodeSnippet(
            """
            navigation(startDestination = "step1", route = "wizard") {
                composable("step1") { entry ->
                    // ViewModel належить графу "wizard" — спільний для всіх кроків
                    val parentEntry = remember(entry) { navController.getBackStackEntry("wizard") }
                    val vm: WizardViewModel = viewModel(parentEntry)
                    Step1Screen(vm)
                }
                composable("step2") { entry -> /* той самий parentEntry → той самий vm */ }
            }
            """,
        )
    }
}
