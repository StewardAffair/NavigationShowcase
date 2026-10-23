package ua.ofkkt.navshowcase.l7

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.unit.dp
import ua.ofkkt.navshowcase.ui.common.ActionRow
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 7 · слайди «Планування навігації проєкту», «Практика: ескіз навігаційного графа»,
 * «Приклад: граф для CRUD-застосунку». Лекція 9 · «Чек-лист міцного скелета» (повний граф, без «тупиків»).
 *
 * Тут граф перевіряється автоматично за критеріями зі слайда:
 *   Повнота · Досяжність · Зворотність · Складність.
 */
private val Screens = listOf("Список", "Деталі", "Редагування", "Налаштування", "Профіль", "Довідка")

private class GraphReport(
    val unreachable: List<String>,
    val deadEnds: List<String>,
    val isolated: List<String>,
    val maxDepth: Int,
)

private fun analyze(edges: List<Pair<Int, Int>>, start: Int = 0): GraphReport {
    val n = Screens.size
    val dist = IntArray(n) { -1 }
    dist[start] = 0
    val queue = ArrayDeque<Int>()
    queue.add(start)
    while (queue.isNotEmpty()) {
        val u = queue.removeFirst()
        for (edge in edges) {
            if (edge.first == u && dist[edge.second] == -1) {
                dist[edge.second] = dist[u] + 1
                queue.add(edge.second)
            }
        }
    }
    val unreachable = Screens.indices.filter { dist[it] == -1 }.map { Screens[it] }
    val deadEnds = Screens.indices
        .filter { dist[it] >= 0 && edges.none { e -> e.first == it } }
        .map { Screens[it] }
    val isolated = Screens.indices
        .filter { i -> i != start && edges.none { it.first == i || it.second == i } }
        .map { Screens[it] }
    return GraphReport(unreachable, deadEnds, isolated, dist.max())
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NavPlanningDemo() {
    val edges = remember { mutableStateListOf(0 to 1, 1 to 2, 2 to 0, 0 to 3) }
    val report = analyze(edges.toList())

    DemoSection(
        "1. Намалюйте граф переходів",
        note = "Стартовий екран — «Список». Для кожного екрана позначте, куди з нього можна перейти (стрілки графа).",
    ) {
        Screens.forEachIndexed { from, name ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("$name →", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Screens.forEachIndexed { dest, target ->
                        if (dest != from) {
                            val edge = Pair(from, dest)
                            FilterChip(
                                selected = edge in edges,
                                onClick = { if (edge in edges) edges.remove(edge) else edges.add(edge) },
                                label = { Text(target) },
                            )
                        }
                    }
                }
            }
        }
        ActionRow(
            listOf(
                "CRUD-шаблон" to {
                    edges.clear()
                    edges.addAll(listOf(0 to 1, 1 to 2, 2 to 0, 0 to 2, 0 to 3, 3 to 0))
                },
                "Граф із помилками" to {
                    edges.clear()
                    edges.addAll(listOf(0 to 1, 1 to 2, 2 to 5, 4 to 0))
                },
                "Очистити" to { edges.clear() },
            ),
        )
    }

    DemoSection(
        "2. Перевірка за критеріями лекції",
        note = "Гарний граф на папері економить години рефакторингу коду пізніше.",
    ) {
        if (report.unreachable.isEmpty()) {
            Tip("Досяжність: на кожен екран можна потрапити зі стартового.", TipKind.Success)
        } else {
            Tip("Досяжність: недосяжні екрани — ${report.unreachable.joinToString()}. Користувач ніколи їх не побачить.", TipKind.Warning)
        }
        if (report.isolated.isEmpty()) {
            Tip("Повнота: усі екрани позначені в графі.", TipKind.Success)
        } else {
            Tip("Повнота: екрани без жодного зв'язку — ${report.isolated.joinToString()}.", TipKind.Warning)
        }
        if (report.deadEnds.isEmpty()) {
            Tip("Зворотність: з кожного екрана є вихід.", TipKind.Success)
        } else {
            Tip(
                "Тупики (вихід лише «Назад»): ${report.deadEnds.joinToString()}. " +
                    "Для екранів деталей це нормально, але перевірте кожен.",
                TipKind.Info,
            )
        }
        if (report.maxDepth > 4) {
            Tip("Складність: глибина стеку ${report.maxDepth}. Занадто глибокий стек (більше 4–5 рівнів) — спростіть граф.", TipKind.Warning)
        } else {
            Tip("Складність: найглибший екран на рівні ${report.maxDepth} — граф не надто заплутаний.", TipKind.Success)
        }
    }

    DemoSection("3. Домашнє завдання лекції") {
        Text(
            "Намалюйте граф власного проєкту й визначте routes для всіх екранів. Результат — схема графа + список routes.",
            style = MaterialTheme.typography.bodyMedium,
        )
        CodeSnippet(
            """
            // Типовий результат домашнього завдання — список маршрутів
            sealed class Route(val route: String) {
                data object List : Route("list")
                data object Detail : Route("detail/{id}")
                data object Edit : Route("edit?id={id}")      // id відсутній → новий запис
            }
            """,
        )
        Tip(
            "Типові помилки: екрани без зворотного переходу, занадто глибокий стек, дублювання екранів у різних частинах графа.",
            TipKind.Info,
        )
    }
}
