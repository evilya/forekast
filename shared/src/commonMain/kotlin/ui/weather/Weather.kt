package ui.weather

import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
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
import data.LocationWeather
import data.WeatherData
import forekast.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import ui.LocalLocationRepositoryProvider
import ui.LocalWeatherApi
import ui.icon


class CurrentWeatherScreen : Screen {

    @Composable
    override fun Content() {
        CurrentWeather()
    }
}


@Composable
fun CurrentWeather() {
    val locationRepository = LocalLocationRepositoryProvider.current
    val weatherApi = LocalWeatherApi.current
    val locations by locationRepository.observeLocations().collectAsState(emptyList())
    var isRefreshing by remember { mutableStateOf(true) }
    var addingLocation by remember { mutableStateOf(false) }
    var weather by remember { mutableStateOf(emptyList<LocationWeather>()) }

    LaunchedEffect(locations, isRefreshing) {
        weather = locations.map { LocationWeather(it, weatherApi.getCurrentWeather(it)) }
        isRefreshing = false
    }

    CurrentWeather(
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
            locations = locations,
            onLocationAdded = { locationRepository.addLocation(it) },
            onDismiss = { addingLocation = false }
        )
    }
}

@Composable
fun CurrentWeather(
    locationsWeather: List<LocationWeather>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onAddLocationClick: () -> Unit,
    onLocationClick: (Location) -> Unit = {},
    onLocationLongClick: (Location) -> Unit = {},
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { onRefresh() }
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLocationClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add location"
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(locationsWeather) { (location, weather) ->
                    LocationWeatherCard(
                        location, weather,
                        onClick = { onLocationClick(location) },
                        onLongClick = { onLocationLongClick(location) }
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}


@Composable
private fun LocationWeatherCard(
    city: Location,
    weather: Result<WeatherData?>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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
                    text = city.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = marqueeModifier
                )
                Text(
                    text = weather.getOrNull()?.current?.weatherCondition?.text ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = marqueeModifier
                )
            }
            weather.onSuccess { weather ->
                if (weather != null) {
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
                }
            }.onFailure {
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