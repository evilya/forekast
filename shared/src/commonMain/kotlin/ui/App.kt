@file:OptIn(ExperimentalMaterialApi::class)

package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
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
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Navigator(CurrentWeatherScreen())
        }
    }
}
