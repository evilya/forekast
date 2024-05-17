package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import di.commonModule
import org.koin.compose.KoinApplication
import ui.screen.CurrentWeatherScreen
import ui.theme.AppTheme

@Composable
fun App() = KoinApplication(
    application = { modules(commonModule) },
) {
    val useDarkTheme by remember { mutableStateOf(false) }
    AppTheme(useDarkTheme) {
        Navigator(CurrentWeatherScreen()) {
            SlideTransition(it)
        }
    }
}
