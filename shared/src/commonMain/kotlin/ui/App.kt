package ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import data.LocationRepository
import data.LocationWeather
import data.WeatherApi
import ui.theme.AppTheme


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
            WeatherApp()
        }
    }
}

@Composable
fun WeatherApp() {
    val locationRepository = LocalLocationRepositoryProvider.current
    val locations by locationRepository.observeLocations().collectAsState(emptyList())
    var addingLocation by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(true) }
    var weather by remember { mutableStateOf(emptyList<LocationWeather>()) }
    val weatherApi = LocalWeatherApi.current

    LaunchedEffect(locations, isRefreshing) {
        weather = locations.map { LocationWeather(it, weatherApi.getCurrentWeather(it)) }
        isRefreshing = false
    }

    CurrentWeatherScreen(
        weather, isRefreshing,
        onRefresh = { isRefreshing = true },
        onAddLocationClick = { addingLocation = true },
        onLocationClick = { location ->
            // todo open location details
        },
        onLocationLongClick = { location ->
            // todo implement swipe to delete with anchored draggable after 1.6.0 merge
            locationRepository.removeLocation(location)
        }
    )

    if (addingLocation) {
        AddLocationBottomSheet(
            locations,
            onLocationAdded = { locationRepository.addLocation(it) },
            onDismiss = { addingLocation = false }
        )
    }
}