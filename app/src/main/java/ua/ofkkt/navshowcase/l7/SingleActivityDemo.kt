package ua.ofkkt.navshowcase.l7

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ua.ofkkt.navshowcase.LegacyDetailActivity
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.CompareTable
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/*
 * Лекція 7 · слайди «Single-Activity парадигма» і «Single-Activity vs Multi-Activity».
 */
@Composable
fun SingleActivityDemo() {
    val context = LocalContext.current

    DemoSection(
        "1. Дві моделі",
        note = "Стара модель: кожен екран — окремий Activity. Нова: один Activity, екрани — composable-функції.",
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Multi-Activity", style = MaterialTheme.typography.titleSmall)
                Text("Activity A → Intent → Activity B", style = MaterialTheme.typography.bodySmall)
                Text("• дублювання lifecycle-коду", style = MaterialTheme.typography.bodySmall)
                Text("• дані через Extras", style = MaterialTheme.typography.bodySmall)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Single-Activity", style = MaterialTheme.typography.titleSmall)
                Text("MainActivity → NavHost → Composable", style = MaterialTheme.typography.bodySmall)
                Text("• єдиний lifecycle для всього UI", style = MaterialTheme.typography.bodySmall)
                Text("• аргументи через route-рядок", style = MaterialTheme.typography.bodySmall)
            }
        }
        CompareTable(
            headers = listOf("Аспект", "Single-Activity", "Multi-Activity"),
            rows = listOf(
                listOf("Навігація", "NavController усередині застосунку", "Intent між різними Activity"),
                listOf("Передача даних", "Аргументи route або спільний ViewModel", "Extras у Intent, Parcelable"),
                listOf("Lifecycle", "Один lifecycle для всього UI", "Кожен Activity має власний"),
                listOf("Анімації переходів", "Легко через Navigation Compose", "overridePendingTransition()"),
                listOf("Deep links", "Вбудована підтримка у графі", "Intent-filter для кожного Activity"),
                listOf("Сучасний стандарт", "✅ Рекомендовано Google", "⚠️ Legacy, не для нових проєктів"),
            ),
        )
    }

    DemoSection(
        "2. Спробуйте старий підхід",
        note = "Кнопка запускає ОКРЕМИЙ Activity через Intent з Extras. Він має власний lifecycle і власну запис у системному back stack.",
    ) {
        Button(onClick = { context.startActivity(LegacyDetailActivity.intent(context, itemId = 42)) }) {
            Text("startActivity(Intent(...).putExtra(\"itemId\", 42))")
        }
        CodeSnippet(
            """
            // Multi-Activity: перехід і передача даних
            val intent = Intent(context, DetailActivity::class.java)
                .putExtra("itemId", 42)
            context.startActivity(intent)

            // у DetailActivity
            val itemId = intent.getIntExtra("itemId", -1)
            """,
        )
    }

    DemoSection("3. Як це зроблено тут") {
        Text(
            "Цей застосунок — Single-Activity: у маніфесті один «основний» MainActivity, а всі демо-екрани " +
                "(понад 20) — звичайні composable-функції, зареєстровані в кореневому NavHost (app/AppRoot.kt).",
            style = MaterialTheme.typography.bodyMedium,
        )
        CodeSnippet(
            """
            class MainActivity : ComponentActivity() {
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    setContent { AppTheme { AppRoot() } }   // MainActivity → NavHost → Composable
                }
            }
            """,
        )
        Tip("Менше Activity — менше дублювання lifecycle-логіки. Один Activity — одна точка входу, вся складність навігації делегується Navigation Compose.", TipKind.Success)
        Tip("Multi-Activity і досі трапляється у старих або дуже модульних проєктах. Знати обидва підходи корисно, але новий код слід писати у Single-Activity стилі.", TipKind.Info)
    }
}
