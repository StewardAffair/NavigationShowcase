@file:OptIn(ExperimentalLayoutApi::class)

package ua.ofkkt.navshowcase.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraph

/**
 * «Нульові» вставки для екранів, вкладених у пісочницю.
 * Кореневий Scaffold уже врахував статус-бар і навігаційну панель, тому внутрішні
 * Scaffold / TopAppBar / NavigationBar не повинні додавати їх удруге.
 */
val NoInsets = WindowInsets(0, 0, 0, 0)

/** Прокручувана колонка — «каркас» для демо-екранів, які мають кілька секцій. */
@Composable
fun DemoPage(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

/** Картка-розділ із заголовком і коротким поясненням. */
@Composable
fun DemoSection(
    title: String,
    modifier: Modifier = Modifier,
    note: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    OutlinedCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            if (note != null) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content()
        }
    }
}

/** Фрагмент коду моноширинним шрифтом (з горизонтальним скролом для довгих рядків). */
@Composable
fun CodeSnippet(code: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .horizontalScroll(rememberScrollState())
            .padding(12.dp),
    ) {
        Text(
            text = code.trimIndent(),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            softWrap = false,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

enum class TipKind { Info, Success, Warning }

@Composable
fun Tip(text: String, kind: TipKind = TipKind.Info, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val (bg, fg) = when (kind) {
        TipKind.Info -> scheme.secondaryContainer to scheme.onSecondaryContainer
        TipKind.Success -> scheme.primaryContainer to scheme.onPrimaryContainer
        TipKind.Warning -> scheme.tertiaryContainer to scheme.onTertiaryContainer
    }
    val icon = when (kind) {
        TipKind.Info -> Icons.Outlined.Info
        TipKind.Success -> Icons.Outlined.CheckCircle
        TipKind.Warning -> Icons.Outlined.Warning
    }
    Row(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(bg)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = fg)
    }
}

/** Таблиця порівняння (як таблиці на слайдах): перший рядок — заголовки. */
@Composable
fun CompareTable(headers: List<String>, rows: List<List<String>>, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.small
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
    ) {
        CompareRow(headers, header = true)
        rows.forEach { row ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            CompareRow(row, header = false)
        }
    }
}

@Composable
private fun CompareRow(cells: List<String>, header: Boolean) {
    val bg = if (header) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent
    Row(
        Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        cells.forEach { cell ->
            Text(
                cell,
                modifier = Modifier.weight(1f),
                style = if (header) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** Набір FilterChip для вибору одного варіанта. */
@Composable
fun <T> ChoiceChips(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    FlowRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) },
            )
        }
    }
}

/** Рядок «текст + Switch»; увесь рядок клікабельний (Role.Switch). */
@Composable
fun LabeledSwitch(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = null)
    }
}

/** Ряд кнопок-дій, що переноситься на новий рядок. */
@Composable
fun ActionRow(actions: List<Pair<String, () -> Any?>>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        actions.forEach { (label, action) ->
            OutlinedButton(onClick = { action() }) { Text(label, style = MaterialTheme.typography.labelMedium) }
        }
    }
}

/* ─────────────────────────── Пісочниця для навігації ─────────────────────────── */

/** Рамка фіксованої висоти — всередині живе власний NavHost із власним NavController. */
@Composable
fun SandboxFrame(modifier: Modifier = Modifier, height: Dp = 260.dp, content: @Composable () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .border(1.5.dp, MaterialTheme.colorScheme.primary, shape)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        content()
    }
}

/** Мінімальний «екран» пісочниці: заголовок, підпис, довільний вміст і кнопки. */
@Composable
fun MiniScreen(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: List<Pair<String, () -> Any?>> = emptyList(),
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        content()
        actions.forEach { (label, action) ->
            OutlinedButton(onClick = { action() }) { Text(label) }
        }
    }
}

/**
 * Текстовий підпис запису back stack: шаблон маршруту з підставленими значеннями аргументів.
 * detail/{id} + id=5  →  detail/5
 */
@Suppress("DEPRECATION")
fun NavBackStackEntry.pretty(): String {
    val dest = destination
    var route = dest.route ?: return "⟦без маршруту⟧"
    if (dest is NavGraph) return "⟦граф: $route⟧"
    val args = arguments
    if (args != null) {
        for (key in args.keySet()) {
            route = route.replace("{$key}", args.get(key).toString())
        }
    }
    return route
}

/** Живий вигляд back stack: верхівка — перший рядок. Лекція 7: «Back stack: як це працює». */
@Composable
fun BackStackViewer(
    navController: NavController,
    modifier: Modifier = Modifier,
    showGraphs: Boolean = false,
) {
    val stack by navController.currentBackStack.collectAsState()
    val entries = stack.filter { showGraphs || it.destination !is NavGraph }.reversed()
    Column(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text("Back stack (верхівка — перший рядок)", style = MaterialTheme.typography.labelLarge)
        if (entries.isEmpty()) {
            Text(
                "порожній → NavHost нічого не показує",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        entries.forEachIndexed { index, entry ->
            val top = index == 0
            Text(
                text = (if (top) "▶ " else "· ") + entry.pretty(),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = if (top) FontWeight.Bold else FontWeight.Normal,
                color = if (top) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
