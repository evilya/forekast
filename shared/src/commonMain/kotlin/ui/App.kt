@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
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
        val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = { addingLocation = false },
            windowInsets = WindowInsets(0.dp)
        ) {
            AddLocation(
                locations = locations,
                onLocationAdded = {
                    if (locations.contains(it)) return@AddLocation
                    locationRepository.addLocation(it)
                    keyboardController?.hide()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(bottom = 16.dp)
            )
            LaunchedEffect(bottomSheetState.currentValue) {
                if (bottomSheetState.currentValue == SheetValue.Expanded) focusRequester.requestFocus()
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}