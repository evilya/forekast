@file:OptIn(ExperimentalMaterial3Api::class)

package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import data.LocationRepository
import data.LocationWeather
import data.WeatherProvider
import ui.theme.AppTheme


val LocalWeatherProvider = staticCompositionLocalOf { WeatherProvider() }

val LocalLocationRepositoryProvider = staticCompositionLocalOf { LocationRepository() }

@Composable
fun App() {
    val useDarkTheme by remember { mutableStateOf(false) }
    AppTheme(useDarkTheme) {
        WeatherApp()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WeatherApp() {
    val locationRepository = LocalLocationRepositoryProvider.current
    val locations by locationRepository.observeLocations().collectAsState(emptyList())
    var addingLocation by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(true) }
    var weather by remember { mutableStateOf(emptyList<LocationWeather>()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val weatherProvider = LocalWeatherProvider.current

    LaunchedEffect(locations, isRefreshing) {
        weather = locations.map { LocationWeather(it, weatherProvider.getWeather(it)) }
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
        val bottomSheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = { addingLocation = false }
        ) {
            AddLocation(
                onLocationAdded = {
                    if (locations.contains(it)) return@AddLocation
                    locationRepository.addLocation(it)
                    keyboardController?.hide()
                },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )
            LaunchedEffect(bottomSheetState.currentValue) {
                when (bottomSheetState.currentValue) {
                    SheetValue.Expanded, SheetValue.PartiallyExpanded -> focusRequester.requestFocus()
                    else -> {}
                }
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}