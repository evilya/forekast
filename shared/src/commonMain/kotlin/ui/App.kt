package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import data.LocationRepository
import data.WeatherApi
import ui.theme.AppTheme
import ui.weather.CurrentWeatherScreen


val LocalWeatherApi = staticCompositionLocalOf { WeatherApi() }

val LocalLocationRepositoryProvider = staticCompositionLocalOf { LocationRepository() }

@Composable
fun App() {
    val useDarkTheme by remember { mutableStateOf(false) }
    AppTheme(useDarkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding()
        ) {
            Navigator(CurrentWeatherScreen())
        }
    }
}
