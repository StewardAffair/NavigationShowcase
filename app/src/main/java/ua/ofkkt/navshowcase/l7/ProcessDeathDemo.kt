package ua.ofkkt.navshowcase.l7

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.StateFlow
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.CompareTable
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.MiniScreen
import ua.ofkkt.navshowcase.ui.common.SandboxFrame
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 7 · слайди «Навігація та process death» і «SavedStateHandle і аргументи навігації».
 * ViewModel отримує аргумент маршруту через SavedStateHandle (Navigation сам його заповнює).
 */
class DetailViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    /** Аргумент маршруту detail/{itemId} доступний у SavedStateHandle автоматично. */
    val itemId: String = savedStateHandle["itemId"] ?: ""

    /** Звичайне поле ViewModel: переживає поворот, але гине разом із процесом. */
    var plainTaps by mutableIntStateOf(0)

    /** Стан у SavedStateHandle: переживає і поворот, і process death. */
    val savedTaps: StateFlow<Int> = savedStateHandle.getStateFlow("taps", 0)

    fun incrementSaved() {
        savedStateHandle["taps"] = savedTaps.value + 1
    }

    val instanceId: Int = System.identityHashCode(this)
}

@Composable
fun ProcessDeathDemo() {
    val nav = rememberNavController()

    DemoSection(
        "1. Чотири способи зберегти лічильник",
        note = "Відкрийте деталі, натисніть кожну кнопку кілька разів, потім повертайте екран або «вбийте» процес.",
    ) {
        SandboxFrame(height = 330.dp) {
            NavHost(navController = nav, startDestination = "list") {
                composable("list") {
                    MiniScreen(
                        title = "Список",
                        actions = listOf("Відкрити detail/5" to { nav.navigate("detail/5") }),
                    )
                }
                composable("detail/{itemId}") { DetailCounters(onBack = { nav.popBackStack() }) }
            }
        }
        CompareTable(
            headers = listOf("Де зберігаємо", "Поворот", "Process death"),
            rows = listOf(
                listOf("remember", "❌ втрачається", "❌ втрачається"),
                listOf("rememberSaveable", "✅ зберігається", "✅ зберігається"),
                listOf("ViewModel (звичайне поле)", "✅ зберігається", "❌ втрачається"),
                listOf("ViewModel + SavedStateHandle", "✅ зберігається", "✅ зберігається"),
            ),
        )
    }

    DemoSection(
        "2. Як перевірити process death",
        note = "Система Android може завершити процес у фоні для звільнення пам'яті. Користувач очікує побачити той самий екран.",
    ) {
        CodeSnippet(
            """
            # варіант 1: Налаштування розробника → «Don't keep activities»
            # варіант 2: згорнути застосунок і вбити процес
            adb shell am kill ua.ofkkt.navshowcase
            # потім відкрити застосунок з Recent apps
            """,
        )
        Tip("Navigation Compose зберігає поточний route при знищенні процесу; аргументи простих типів — String, Int, Boolean — відновлюються автоматично.", TipKind.Success)
    }

    DemoSection("3. Код: аргумент маршруту у ViewModel") {
        CodeSnippet(
            """
            // у NavGraph
            composable("detail/{itemId}") {
                val viewModel: DetailViewModel = viewModel()
                DetailScreen(viewModel)
            }

            // у ViewModel — аргумент вже лежить у SavedStateHandle
            class DetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
                val itemId: String = savedStateHandle["itemId"] ?: ""
                // завантажуємо дані для цього ID
            }
            """,
        )
        Tip("Передавайте тільки ID. Складні об'єкти — це ризик TransactionTooLargeException; дані довантажуються на екрані через ViewModel.", TipKind.Warning)
    }
}

@Composable
private fun DetailCounters(onBack: () -> Unit) {
    val vm: DetailViewModel = viewModel()
    val savedTaps by vm.savedTaps.collectAsState()
    var rememberTaps by remember { mutableIntStateOf(0) }
    var saveableTaps by rememberSaveable { mutableStateOf(0) }

    MiniScreen(
        title = "Деталі #${vm.itemId}",
        subtitle = "ViewModel #${vm.instanceId}",
        actions = listOf(
            "remember: $rememberTaps" to { rememberTaps++ },
            "rememberSaveable: $saveableTaps" to { saveableTaps++ },
            "ViewModel (поле): ${vm.plainTaps}" to { vm.plainTaps++ },
            "SavedStateHandle: $savedTaps" to { vm.incrementSaved() },
            "← Назад" to onBack,
        ),
    ) {
        Text(
            "itemId прийшов із маршруту через SavedStateHandle",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
