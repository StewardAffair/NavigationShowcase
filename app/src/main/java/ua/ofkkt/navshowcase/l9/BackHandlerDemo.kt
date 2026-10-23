package ua.ofkkt.navshowcase.l9

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ua.ofkkt.navshowcase.ui.common.BackStackViewer
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.MiniScreen
import ua.ofkkt.navshowcase.ui.common.SandboxFrame
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 9 · слайд «Обробка системної кнопки "Назад"»: BackHandler(enabled = hasUnsavedChanges) { … }.
 * Перехоплення активне, лише коли форма справді має незбережені зміни.
 */
@Composable
fun BackHandlerDemo() {
    val nav = rememberNavController()

    DemoSection(
        "1. Форма з незбереженими змінами",
        note = "Відкрийте форму, наберіть текст і натисніть системну «Назад» (або стрілку на формі) — з'явиться діалог. Після «Зберегти» «Назад» знову працює як звичайно.",
    ) {
        SandboxFrame(height = 300.dp) {
            NavHost(navController = nav, startDestination = "list") {
                composable("list") {
                    MiniScreen(
                        title = "Список",
                        actions = listOf("Відкрити форму" to { nav.navigate("edit") }),
                    )
                }
                composable("edit") {
                    EditFormScreen(onClose = { nav.popBackStack() })
                }
            }
        }
        BackStackViewer(nav)
    }

    DemoSection("2. Код") {
        CodeSnippet(
            """
            @Composable
            fun EditScreen(hasUnsavedChanges: Boolean) {
                var showDialog by remember { mutableStateOf(false) }

                // активний, доки composable у композиції; вмикається параметром enabled
                BackHandler(enabled = hasUnsavedChanges) {
                    showDialog = true
                }

                if (showDialog) {
                    DiscardChangesDialog(
                        onConfirm = { navController.popBackStack() },
                        onDismiss = { showDialog = false },
                    )
                }
            }
            """,
        )
        Text("Типові сценарії: форма з незбереженими змінами · «кошик» у грі (повернути до паузи) · Bottom Sheet (закрити аркуш замість навігації назад).", style = MaterialTheme.typography.bodySmall)
        Tip("Перехоплюйте «Назад» лише там, де це справді потрібно користувачу. Надмірне перехоплення псує очікувану UX-поведінку.", TipKind.Warning)
    }
}

@Composable
private fun EditFormScreen(onClose: () -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    var saved by rememberSaveable { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    val hasUnsavedChanges = text != saved

    BackHandler(enabled = hasUnsavedChanges) { showDialog = true }

    Column(
        Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Редагування", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Нотатка") },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            if (hasUnsavedChanges) "● Є незбережені зміни — BackHandler активний" else "✓ Змін немає — BackHandler вимкнено",
            style = MaterialTheme.typography.bodySmall,
            color = if (hasUnsavedChanges) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { saved = text }, enabled = hasUnsavedChanges) { Text("Зберегти") }
            // Стрілка «Назад» у TopAppBar має поводитись так само, як системна кнопка
            OutlinedButton(onClick = { if (hasUnsavedChanges) showDialog = true else onClose() }) {
                Text("← Назад")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Відхилити зміни?") },
            text = { Text("Незбережений текст буде втрачено.") },
            confirmButton = {
                TextButton(onClick = { showDialog = false; onClose() }) { Text("Відхилити") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Лишитися") }
            },
        )
    }
}
