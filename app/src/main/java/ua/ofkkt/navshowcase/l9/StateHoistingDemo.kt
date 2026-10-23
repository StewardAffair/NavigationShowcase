package ua.ofkkt.navshowcase.l9

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.CompareTable
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 9 · слайд «State Hoisting: основи» — stateful vs stateless composable,
 * пара параметрів value + onValueChange, «аналогія з TextField».
 */
@Composable
fun StateHoistingDemo() {
    // ↓ Стан піднято у «найближчого спільного предка»
    var hoistedCount by rememberSaveable { mutableStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }

    DemoSection(
        "1. Stateful: стан усередині",
        note = "Лічильник тримає свій стан сам. Батько не може ні прочитати його, ні скинути. Поверніть екран — значення зникне (remember).",
    ) {
        StatefulCounter()
        Text("Батько не знає, що всередині 🤷", style = MaterialTheme.typography.bodySmall)
    }

    DemoSection(
        "2. Stateless: стан піднято",
        note = "Той самий візуальний компонент, але value + onIncrement приходять ззовні. Батько бачить стан, може його скинути і показати в іншому місці.",
    ) {
        StatelessCounter(value = hoistedCount, onIncrement = { hoistedCount++ })
        StatelessCounter(value = hoistedCount, onIncrement = { hoistedCount += 10 }, step = 10)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Батько бачить: $hoistedCount", style = MaterialTheme.typography.titleSmall)
            OutlinedButton(onClick = { hoistedCount = 0 }) { Text("Скинути ззовні") }
        }
        Tip("Два лічильники синхронні, бо читають одне й те саме значення. Стан піднято в rememberSaveable — переживає поворот.", TipKind.Success)
    }

    DemoSection(
        "3. Аналогія: TextField",
        note = "TextField не зберігає текст сам — value + onValueChange. Тому батько завжди знає актуальний стан і може його валідувати.",
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Ім'я") },
            isError = name.isBlank(),
            supportingText = { Text(if (name.isBlank()) "Батько бачить, що поле порожнє" else "Привіт, $name!") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = {}, enabled = name.isNotBlank()) { Text("Продовжити") }
    }

    DemoSection("4. Порівняння підходів") {
        CompareTable(
            headers = listOf("Stateful", "Stateless (hoisted)"),
            rows = listOf(
                listOf("Стан всередині composable", "Стан у предку або ViewModel"),
                listOf("Важко тестувати", "Легко тестувати ізольовано"),
                listOf("Важко перевикористати", "Можна використати в будь-якому контексті"),
                listOf("Стан втрачається при реконфігурації", "Стан контролюється ззовні"),
            ),
        )
        CodeSnippet(
            """
            @Composable
            fun StatelessCounter(value: Int, onIncrement: () -> Unit) { /* лише відображення */ }

            // стан піднято до найближчого спільного предка
            var count by rememberSaveable { mutableStateOf(0) }
            StatelessCounter(value = count, onIncrement = { count++ })
            """,
        )
        Tip("«Composable, що керує власним станом, важче повторно використати в іншому місці». Це готує ґрунт для MVVM у блоці 4: ViewModel стає «піднятим» місцем зберігання стану для цілого екрана.", TipKind.Info)
    }
}

@Composable
private fun StatefulCounter() {
    var count by remember { mutableIntStateOf(0) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = { count++ }) { Text("+1") }
        Text("Власний стан: $count")
    }
}

@Composable
private fun StatelessCounter(value: Int, onIncrement: () -> Unit, step: Int = 1) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = onIncrement) { Text("+$step") }
        Text("Значення ззовні: $value")
    }
}
