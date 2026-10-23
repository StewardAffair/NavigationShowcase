@file:OptIn(ExperimentalMaterial3Api::class)

package ua.ofkkt.navshowcase

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ua.ofkkt.navshowcase.ui.common.CodeSnippet
import ua.ofkkt.navshowcase.ui.theme.NavShowcaseTheme

/**
 * «Старий» підхід Multi-Activity (Лекція 7, слайд «Single-Activity vs Multi-Activity»):
 * окремий Activity, дані передаються через Intent Extras, власний lifecycle і власний запис у системному back stack.
 */
class LegacyDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val itemId = intent.getIntExtra(EXTRA_ITEM_ID, -1)
        setContent {
            NavShowcaseTheme {
                LegacyDetailScreen(itemId = itemId, onClose = { finish() })
            }
        }
    }

    companion object {
        const val EXTRA_ITEM_ID = "itemId"

        fun intent(context: Context, itemId: Int): Intent =
            Intent(context, LegacyDetailActivity::class.java).putExtra(EXTRA_ITEM_ID, itemId)
    }
}

@Composable
private fun LegacyDetailScreen(itemId: Int, onClose: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Окремий Activity") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Закрити")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            Modifier.padding(inner).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("itemId = $itemId (отримано через Intent Extras)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Це окремий Activity: власний lifecycle, власна точка входу в маніфесті, " +
                    "власний запис у системному back stack. Кнопка «Назад» закриває його (finish()), " +
                    "а не знімає екран з NavController.",
                style = MaterialTheme.typography.bodyMedium,
            )
            CodeSnippet(
                """
                val itemId = intent.getIntExtra("itemId", -1)

                // перевірити окремі Activity у системному стеку:
                // adb shell dumpsys activity activities | grep ua.ofkkt.navshowcase
                """,
            )
        }
    }
}
