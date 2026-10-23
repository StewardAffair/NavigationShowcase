package ua.ofkkt.navshowcase.l9

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ua.ofkkt.navshowcase.data.SampleData
import ua.ofkkt.navshowcase.ui.common.ActionRow
import ua.ofkkt.navshowcase.ui.common.AppCard
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.common.CompareTable
import ua.ofkkt.navshowcase.ui.common.DemoSection
import ua.ofkkt.navshowcase.ui.common.Tip
import ua.ofkkt.navshowcase.ui.common.TipKind

/**
 * Лекція 9 · слайд «Адаптивні макети: базово»: BoxWithConstraints (maxWidth/maxHeight),
 * breakpoints Material 3 (до 600 dp / 600–840 / 840+), LocalConfiguration.orientation, відносні відступи.
 */
private enum class WidthClass(val label: String) {
    Compact("Compact (< 600 dp, телефон)"),
    Medium("Medium (600–840 dp, планшет у портреті)"),
    Expanded("Expanded (≥ 840 dp, планшет у ландшафті)"),
}

@Composable
fun AdaptiveLayoutsDemo() {
    var simulatedWidth by rememberSaveable { mutableStateOf(360f) }
    val config = LocalConfiguration.current

    DemoSection(
        "1. BoxWithConstraints: розрізняємо compact і решту",
        note = "Контейнер нижче «прикидається» екраном заданої ширини (його можна гортати вбік). Змініть ширину — макет перемикається.",
    ) {
        Slider(value = simulatedWidth, onValueChange = { simulatedWidth = it }, valueRange = 320f..1000f)
        Text("Ширина контейнера: ${simulatedWidth.toInt()} dp", style = MaterialTheme.typography.labelLarge)
        ActionRow(
            listOf(
                "360 (телефон)" to { simulatedWidth = 360f },
                "700 (планшет, портрет)" to { simulatedWidth = 700f },
                "960 (планшет, ландшафт)" to { simulatedWidth = 960f },
            ),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            AdaptiveSample(Modifier.width(simulatedWidth.dp).height(230.dp))
        }
    }

    DemoSection("2. Орієнтація та розмір екрана пристрою") {
        val landscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
        Text("Орієнтація: ${if (landscape) "ландшафтна" else "портретна"}", style = MaterialTheme.typography.bodyMedium)
        Text("screenWidthDp = ${config.screenWidthDp} · screenHeightDp = ${config.screenHeightDp}", style = MaterialTheme.typography.bodyMedium)
        Text("Поверніть пристрій — значення зміняться. Для простих коригувань макета цього достатньо.", style = MaterialTheme.typography.bodySmall)
        CodeSnippet(
            """
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            """,
        )
    }

    DemoSection("3. Breakpoints Material 3") {
        CompareTable(
            headers = listOf("Клас", "Ширина", "Пристрої"),
            rows = listOf(
                listOf("Compact", "до 600 dp", "телефон"),
                listOf("Medium", "600–840 dp", "планшет у портреті"),
                listOf("Expanded", "840 dp і більше", "планшет у ландшафті, desktop"),
            ),
        )
        CodeSnippet(
            """
            BoxWithConstraints {
                if (maxWidth > 600.dp) TabletLayout() else PhoneLayout()
            }

            // Preview для різних розмірів
            @Preview(device = Devices.TABLET)
            @Composable fun ListPreview() { … }
            """,
        )
        Tip("Використовуйте відносні відступи (fillMaxWidth(0.9f), weight) замість фіксованих dp для кращого масштабування. Навіть проста перевірка ширини екрана покращує вигляд на планшетах.", TipKind.Success)
    }
}

@Composable
private fun AdaptiveSample(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        // Копіюємо значення зі scope: усередині Column/Row неявним отримувачем буде вже інший scope.
        val width: Dp = maxWidth
        val widthClass = when {
            width < 600.dp -> WidthClass.Compact
            width < 840.dp -> WidthClass.Medium
            else -> WidthClass.Expanded
        }
        Column(Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "maxWidth = ${width.value.toInt()} dp → ${widthClass.label}",
                style = MaterialTheme.typography.labelMedium,
            )
            if (widthClass == WidthClass.Compact) {
                // Телефон: лише список
                ListPane(Modifier.weight(1f))
            } else {
                // Планшет: список + деталі поруч
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ListPane(Modifier.weight(1f))
                    DetailPane(Modifier.weight(if (widthClass == WidthClass.Expanded) 2f else 1f))
                }
            }
        }
    }
}

@Composable
private fun ListPane(modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SampleData.courses.take(3).forEach { AppCard(title = it.title, contentPadding = 8.dp) }
    }
}

@Composable
private fun DetailPane(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(12.dp),
    ) {
        Text("Деталі", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text("Панель деталей з'являється лише на широких екранах.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
