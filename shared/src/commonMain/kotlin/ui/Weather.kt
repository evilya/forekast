package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import data.Location
import data.WeatherData
import forekast.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import ui.DragAnchors.End

typealias WeatherResult = Result<WeatherData>

class CurrentWeatherScreen : Screen {

    @Composable
    override fun Content() {
        CurrentWeather()
    }
}


@Composable
fun CurrentWeather() {
    val locationRepository = LocalLocationRepositoryProvider.current
    val locations by locationRepository.observeLocations().collectAsState(emptyList())
    var addingLocation by remember { mutableStateOf(false) }

    CurrentWeather(
        locations = locations,
        onAddLocationClick = { addingLocation = true },
        onLocationClick = { location ->
            // todo open location details
        },
        onLocationDelete = { location ->
            locationRepository.removeLocation(location)
        }
    )

    if (addingLocation) {
        AddLocationBottomSheet(
            locations = locations,
            onLocationAdded = { locationRepository.addLocation(it) },
            onDismiss = { addingLocation = false }
        )
    }
}

@Composable
fun CurrentWeather(
    locations: List<Location>,
    onAddLocationClick: () -> Unit,
    onLocationClick: (Location) -> Unit = {},
    onLocationDelete: (Location) -> Unit = {},
) {
    Scaffold(
        floatingActionButton = {
            if (locations.isNotEmpty()) {
                FloatingActionButton(onClick = onAddLocationClick) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add location"
                    )
                }
            }
        }
    ) {
        if (locations.isNotEmpty()) {
            LocationsList(
                locations = locations,
                onLocationClick = onLocationClick,
                onLocationDelete = onLocationDelete
            )
        } else {
            EmptyLocations(onAddLocationClick = onAddLocationClick)
        }
    }
}

@Composable
private fun LocationsList(
    locations: List<Location>,
    onLocationClick: (Location) -> Unit,
    onLocationDelete: (Location) -> Unit
) {
    val weatherApi = LocalWeatherApi.current
    val weather = remember { mutableStateMapOf<Location, WeatherResult?>() }
    val isRefreshing = weather.values.any { it == null }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            weather.clear()
            locations.associateWithTo(weather) { null }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(locations) { location ->
                LaunchedEffect(isRefreshing) {
                    if (weather[location] == null) {
                        weather[location] = weatherApi.getCurrentWeather(location)
                    }
                }

                key(location.id) {
                    LocationWeatherCard(
                        location = location,
                        weather = weather[location],
                        onClick = { onLocationClick(location) },
                        onDelete = { onLocationDelete(location) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun EmptyLocations(onAddLocationClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Image(
                imageVector = vectorResource(Res.drawable.earth),
                contentDescription = "Weather icon",
                modifier = Modifier.size(100.dp)
            )
            OutlinedButton(
                onClick = onAddLocationClick
            ) {
                Text(stringResource(Res.string.add_location))
            }
        }
    }
}

@Composable
private fun LocationWeatherCard(
    location: Location,
    weather: WeatherResult?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    DragToDelete(
        modifier = modifier,
        shape = CardDefaults.shape,
        onValueChanged = {
            if (it == End) onDelete()
            true
        }
    ) {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            onClick = onClick
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                val marqueeModifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    delayMillis = 0,
                    spacing = MarqueeSpacing(8.dp),
                    velocity = 60.dp
                )
                Column(modifier = Modifier.weight(2f)) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = marqueeModifier
                    )
                    Text(
                        text = weather?.getOrNull()?.current?.weatherCondition?.text ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = marqueeModifier
                    )
                }
                weather?.onSuccess { weather ->
                    weather.current.weatherCondition.code?.icon?.let { weatherIcon ->
                        Image(
                            imageVector = vectorResource(weatherIcon),
                            contentDescription = "Weather icon",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(60.dp).weight(1f)
                        )
                    }
                    val temperatureUnit = stringResource(Res.string.unit_celsius)
                    Text(
                        text = "${weather.current.temperature.toInt()}$temperatureUnit",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(80.dp).weight(1f)
                    )
                }?.onFailure {
                    Image(
                        imageVector = vectorResource(Res.drawable.ic_error),
                        contentDescription = "Error icon",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(80.dp),
                    )
                }
            }
        }
    }
}

