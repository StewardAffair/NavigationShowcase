package ua.ofkkt.navshowcase.l9

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ua.ofkkt.navshowcase.ui.common.CompareTable
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 9 · слайди «Типові помилки багатоекранних застосунків», «Чек-лист міцного скелета застосунку»,
 * «Практика: скелет навігації проєкту» і домашнє завдання.
 */
private class CheckItem(val title: String, val description: String, val whereHere: String)

private val Checklist = listOf(
    CheckItem(
        "Екрани як окремі composable",
        "Кожен екран — окрема composable-функція з чітким набором параметрів (стан + колбеки). Назва відображає призначення: HomeScreen, ProfileScreen, OrderDetailScreen. Жодної бізнес-логіки всередині — лише відображення.",
        "crud/CourseListScreen.kt (Screen — stateless) + CourseListRoute (stateful)",
    ),
    CheckItem(
        "Повний навігаційний граф",
        "Граф покриває всі заплановані переходи між екранами. Навіть якщо деякі екрани ще порожні — вони зареєстровані в NavHost. Ніяких «навігаційних тупиків».",
        "app/AppRoot.kt + crud/CrudApp.kt; перевірка графа — екран «Планування навігації»",
    ),
    CheckItem(
        "Консистентна тема",
        "MaterialTheme застосована на рівні кореневого composable і охоплює весь застосунок, включно з темним режимом. Жодних хардкодованих кольорів — лише MaterialTheme.colorScheme.primary тощо.",
        "ui/theme/Theme.kt; перемкніть системну темну тему й подивіться на будь-який екран",
    ),
    CheckItem(
        "Обробка всіх UI-станів",
        "Loading, Success, Error та Empty стани передбачені й реалізовані для кожного екрана з даними. Користувач ніколи не бачить порожнього нічим не поясненого екрана.",
        "data/Models.kt (ScreenUiState) + ui/common/AppComponents.kt (UiStateContent)",
    ),
)

@Composable
fun SkeletonChecklistDemo() {
    // Біти: i-й біт = i-й пункт виконано (простий спосіб зберегти набір прапорців у rememberSaveable)
    var mask by rememberSaveable { mutableStateOf(0) }
    val done = Checklist.indices.count { (mask shr it) and 1 == 1 }

    DemoSection(
        "1. Чек-лист міцного скелета застосунку",
        note = "Пройдіться ним щодо власного проєкту ПЕРШ НІЖ переходити до бізнес-логіки. Нижче — де кожен пункт реалізовано в цьому застосунку.",
    ) {
        LinearProgressIndicator(progress = { done / Checklist.size.toFloat() }, modifier = Modifier.fillMaxWidth())
        Text("Виконано $done з ${Checklist.size}", style = MaterialTheme.typography.labelLarge)
        Checklist.forEachIndexed { index, item ->
            val checked = (mask shr index) and 1 == 1
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = checked,
                        role = Role.Checkbox,
                        onValueChange = { mask = mask xor (1 shl index) },
                    ),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Checkbox(checked = checked, onCheckedChange = null)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(item.title, style = MaterialTheme.typography.titleSmall)
                    Text(item.description, style = MaterialTheme.typography.bodySmall)
                    Text("📍 ${item.whereHere}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        if (done == Checklist.size) {
            Tip("Фундамент надійний: можна переходити до бізнес-логіки в блоці 4.", TipKind.Success)
        }
    }

    DemoSection("2. Три найпоширеніші помилки на код-рев'ю") {
        CompareTable(
            headers = listOf("Помилка", "Наслідок", "Правильний підхід"),
            rows = listOf(
                listOf("⚠️ Стан у composable, не піднятий вище", "Втрата даних при перекомпозиції, неможливість читання стану ззовні, проблеми з тестуванням", "Піднімати стан до найближчого спільного предка або ViewModel"),
                listOf("⚠️ Дублювання UI-коду між екранами", "Складна підтримка, розбіжності стилю, більше місць для виправлення", "Виносити спільні елементи у перевикористовувані composable"),
                listOf("⚠️ Відсутність Loading/Error станів", "«Замерзлий» UI при повільній мережі, порожній екран без пояснень, немає «Повторити»", "Явно моделювати всі стани через sealed class і обробляти кожен у UI"),
            ),
        )
    }

    DemoSection("3. Практика: скелет навігації проєкту (чотири кроки)") {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("1. Порожні composable для кожного екрана: Box(Modifier.fillMaxSize()) { Text(\"HomeScreen\") }", style = MaterialTheme.typography.bodySmall)
            Text("2. Підключіть їх до NavHost згідно з навігаційним графом (маршрути унікальні й коректно визначені).", style = MaterialTheme.typography.bodySmall)
            Text("3. Додайте Scaffold з TopAppBar до кожного екрана; на екранах «глибшого рівня» — кнопку «Назад».", style = MaterialTheme.typography.bodySmall)
            Text("4. Вручну перевірте кожен запланований перехід: вперед, далі, назад. Переконайтеся, що «Назад» працює коректно на кожному екрані.", style = MaterialTheme.typography.bodySmall)
        }
        Tip("Готовий скелет навігації — обов'язкова умова для виконання Лаб2. Зробіть це, поки матеріал свіжий.", TipKind.Info)
    }
}
