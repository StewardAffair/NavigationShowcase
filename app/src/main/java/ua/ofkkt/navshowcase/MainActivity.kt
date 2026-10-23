package ua.ofkkt.navshowcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ua.ofkkt.navshowcase.app.AppRoot
import ua.ofkkt.navshowcase.ui.theme.NavShowcaseTheme

/**
 * Лекція 7, слайд «Single-Activity парадигма»: MainActivity → NavHost → Composable.
 * Єдиний Activity з навігацією; другий (LegacyDetailActivity) існує лише для порівняння.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NavShowcaseTheme {
                AppRoot()
            }
        }
    }
}
