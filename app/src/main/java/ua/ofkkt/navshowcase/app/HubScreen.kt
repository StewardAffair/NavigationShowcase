@file:OptIn(ExperimentalMaterial3Api::class)

package ua.ofkkt.navshowcase.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Кореневий екран-каталог. Лекція 9: «TopAppBar — на рівні екрана»: власний Scaffold з шапкою.
 * Екран stateless — лише отримує колбек onOpen(route).
 */
@Composable
fun HubScreen(onOpen: (String) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Navigation Showcase") }) },
    ) { inner ->
        LazyColumn(
            modifier = Modifier.padding(inner).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Card(
                    Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Мобільна розробка: Android + KMP", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Тестовий застосунок до Лекції 7 «Навігація (огляд), структура екранів» і " +
                                "Лекції 9 «Побудова багатоекранних застосунків». Кожен екран нижче демонструє окремий " +
                                "аспект лекцій; угорі кожного екрана вказано, які слайди він ілюструє і що спробувати.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            "Сам застосунок — приклад теми: Single-Activity + кореневий NavHost, де кожен екран-демо — окремий destination.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            Lecture.entries.forEach { lecture ->
                val group = Demos.filter { it.lecture == lecture }
                item(key = "header_${lecture.name}") {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(lecture.label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        Text(lecture.title, style = MaterialTheme.typography.titleMedium)
                    }
                }
                items(group, key = { it.route }) { demo ->
                    ListItem(
                        modifier = Modifier.clickable { onOpen(demo.route) },
                        leadingContent = { Icon(demo.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        headlineContent = { Text(demo.title) },
                        supportingContent = { Text(demo.summary) },
                        trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
                        colors = ListItemDefaults.colors(),
                    )
                }
            }
        }
    }
}
