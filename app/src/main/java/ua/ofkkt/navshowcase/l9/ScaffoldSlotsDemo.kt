@file:OptIn(ExperimentalMaterial3Api::class)

package ua.ofkkt.navshowcase.l9

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ua.ofkkt.navshowcase.ui.common.ChoiceChips
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.CompareTable
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.LabeledSwitch
import ua.ofkkt.navshowcase.ui.common.NoInsets
import ua.ofkkt.navshowcase.ui.common.SandboxFrame
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 9 · слайди «Scaffold: каркас екрана» і «TopAppBar та BottomBar».
 * Слоти: topBar, bottomBar, floatingActionButton, snackbarHost, content (з innerPadding).
 */
private enum class TopBarKind(val label: String) {
    None("Без topBar"),
    Small("TopAppBar"),
    Medium("MediumTopAppBar"),
    Large("LargeTopAppBar"),
}

@Composable
fun ScaffoldSlotsDemo() {
    var kind by rememberSaveable { mutableStateOf(TopBarKind.Small) }
    var bottomBar by rememberSaveable { mutableStateOf(true) }
    var fab by rememberSaveable { mutableStateOf(true) }
    var applyPadding by rememberSaveable { mutableStateOf(true) }

    DemoSection(
        "1. Увімкніть і вимкніть слоти",
        note = "Scaffold сам розраховує відступи під панелі. Прокрутіть список у рамці: Medium/Large шапки згортаються при скролі.",
    ) {
        ChoiceChips(TopBarKind.entries, kind, { kind = it }, { it.label })
        LabeledSwitch("bottomBar (NavigationBar)", bottomBar, { bottomBar = it })
        LabeledSwitch("floatingActionButton", fab, { fab = it })
        LabeledSwitch("застосувати innerPadding до content", applyPadding, { applyPadding = it })
        SandboxFrame(height = 440.dp) {
            ScaffoldSandbox(kind, bottomBar, fab, applyPadding)
        }
        Tip(
            "Вимкніть «innerPadding»: початок списку сховається під шапкою, а кінець — під нижньою панеллю. " +
                "Саме для цього Scaffold передає PaddingValues у content — їх треба застосувати.",
            TipKind.Warning,
        )
        Tip("Натисніть FAB: Scaffold правильно розміщує SnackbarHost над bottomBar і FAB.", TipKind.Info)
    }

    DemoSection("2. Чотири слоти Scaffold") {
        CompareTable(
            headers = listOf("Слот", "Призначення"),
            rows = listOf(
                listOf("topBar", "TopAppBar — заголовок, «Назад», дії (пошук, меню)"),
                listOf("bottomBar", "NavigationBar — основні розділи; зазвичай лише на кореневих екранах"),
                listOf("floatingActionButton", "FAB — основна дія екрана; розміщується з урахуванням bottomBar"),
                listOf("content", "Основний вміст; отримує PaddingValues → Modifier.padding(innerPadding)"),
            ),
        )
        CodeSnippet(
            """
            Scaffold(
                topBar = { TopAppBar(title = { Text("Курси") }) },
                bottomBar = { NavigationBar { /* ... */ } },
                floatingActionButton = { FloatingActionButton(onClick = onAdd) { /* ... */ } },
                snackbarHost = { SnackbarHost(snackbarHostState) },
            ) { innerPadding ->
                CourseList(Modifier.padding(innerPadding))
            }
            """,
        )
    }

    DemoSection(
        "3. TopAppBar — на рівні екрана, BottomBar — на рівні NavHost",
        note = "Це принципова різниця в архітектурі.",
    ) {
        Text("У Material 3 є TopAppBar, MediumTopAppBar, LargeTopAppBar — останні два підтримують колапс при прокрутці.")
        CodeSnippet(
            """
            Scaffold(bottomBar = { NavigationBar { … } }) { padding ->     // рівень кореневого NavHost
                NavHost(navController, "home", Modifier.padding(padding)) {
                    composable("home") { HomeScreen() }                     // кожен екран:
                }                                                           //   Scaffold(topBar = …) { … }
            }
            """,
        )
        Tip("Привʼязуйте TopAppBar до конкретного екрана — так кожен екран має свій заголовок і набір дій. BottomBar, навпаки, однакова на всіх основних екранах і зникає в «глибоких» підекранах.", TipKind.Success)
    }
}

@Composable
private fun ScaffoldSandbox(kind: TopBarKind, showBottomBar: Boolean, showFab: Boolean, applyPadding: Boolean) {
    val scrollBehavior: TopAppBarScrollBehavior = when (kind) {
        TopBarKind.Medium, TopBarKind.Large -> TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        else -> TopAppBarDefaults.pinnedScrollBehavior()
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = NoInsets,
        topBar = {
            when (kind) {
                TopBarKind.None -> {}
                TopBarKind.Small -> TopAppBar(
                    title = { Text("Курси") },
                    windowInsets = NoInsets,
                    scrollBehavior = scrollBehavior,
                )
                TopBarKind.Medium -> MediumTopAppBar(
                    title = { Text("Курси") },
                    windowInsets = NoInsets,
                    scrollBehavior = scrollBehavior,
                )
                TopBarKind.Large -> LargeTopAppBar(
                    title = { Text("Курси") },
                    windowInsets = NoInsets,
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(windowInsets = NoInsets) {
                    NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Filled.Home, null) }, label = { Text("Головна") })
                    NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Filled.Search, null) }, label = { Text("Пошук") })
                    NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Filled.Person, null) }, label = { Text("Профіль") })
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        scope.launch { snackbarHostState.showSnackbar("Scaffold розмістив SnackbarHost над панелями") }
                    },
                ) { Icon(Icons.Filled.Add, contentDescription = "Додати") }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(modifier = if (applyPadding) Modifier.padding(innerPadding) else Modifier) {
            items((1..30).toList()) { n ->
                ListItem(
                    headlineContent = { Text("Елемент списку $n") },
                    supportingContent = { Text("Другий рядок для висоти") },
                )
            }
        }
    }
}
