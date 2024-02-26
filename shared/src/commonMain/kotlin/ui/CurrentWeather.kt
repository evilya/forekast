package ui

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import data.Location
import data.LocationRepository
import data.WeatherApi
import data.WeatherData
import forekast.shared.generated.resources.*
import forekast.shared.generated.resources.Res
import forekast.shared.generated.resources.add_location
import forekast.shared.generated.resources.earth
import forekast.shared.generated.resources.unit_celsius
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import ui.core.DragAnchors.End
import ui.core.DragToDelete
import ui.core.animatedItemsIndexed
import ui.core.updateAnimatedItemsState
import kotlin.collections.List
import kotlin.collections.associateWithTo
import kotlin.collections.emptyList
import kotlin.collections.isNotEmpty
import kotlin.collections.set

typealias WeatherResult = Result<WeatherData>

class CurrentWeatherScreenModel(private val locationRepository: LocationRepository) : ScreenModel {
    val locations = locationRepository.observeLocations()

    fun addLocation(location: Location) {
        locationRepository.addLocation(location)
    }

    fun removeLocation(location: Location) {
        locationRepository.removeLocation(location)
    }
}

class CurrentWeatherScreen : Screen {

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<CurrentWeatherScreenModel>()
        val navigator = LocalNavigator.currentOrThrow

        val locations by screenModel.locations.collectAsState(emptyList())
        var addingLocation by remember { mutableStateOf(false) }

        CurrentWeather(
            locations = locations,
            onLocationAdd = { addingLocation = true },
            onLocationClick = { location ->
                // todo open location details
                // navigator.push(WeatherDetailsScreen(location.id))
            },
            onLocationDelete = { location ->
                screenModel.removeLocation(location)
            }
        )

        if (addingLocation) {
            AddLocationBottomSheet(
                locations = locations,
                onLocationAdded = { screenModel.addLocation(it) },
                onDismiss = { addingLocation = false }
            )
        }
    }
}

@Composable
fun CurrentWeather(
    locations: List<Location>,
    onLocationAdd: () -> Unit,
    onLocationClick: (Location) -> Unit = {},
    onLocationDelete: (Location) -> Unit = {},
) {
    Scaffold(
        floatingActionButton = {
            if (locations.isNotEmpty()) {
                FloatingActionButton(onClick = onLocationAdd) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add location"
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { contentPadding ->
        LocationsList(
            locations = locations,
            onLocationClick = onLocationClick,
            onLocationAdd = onLocationAdd,
            onLocationDelete = onLocationDelete,
            contentPadding = contentPadding
        )
    }
}

@Composable
private fun LocationsList(
    locations: List<Location>,
    onLocationClick: (Location) -> Unit,
    onLocationAdd: () -> Unit,
    onLocationDelete: (Location) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val weatherApi = koinInject<WeatherApi>()
    val weather = remember { mutableStateMapOf<Location, WeatherResult?>() }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = false,
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
        val itemsState by updateAnimatedItemsState(locations)

        if (itemsState.any { it.visibility.currentState }) {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = contentPadding
            ) {
                animatedItemsIndexed(
                    state = itemsState,
                    key = { it.id },
                    enterTransition = slideInHorizontally(initialOffsetX = { -it }),
                    exitTransition = slideOutHorizontally(targetOffsetX = { -it })
                ) { _, location ->
                    LaunchedEffect(weather[location]) {
                        if (weather[location] == null) {
                            weather[location] = weatherApi.getCurrentWeather(location)
                        }
                    }

                    LocationWeatherCard(
                        location = location,
                        weather = weather[location],
                        onClick = { onLocationClick(location) },
                        onDelete = { onLocationDelete(location) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .animateItemPlacement()
                    )
                }
            }
        } else {
            EmptyLocations(onLocationAdd)
        }

        PullRefreshIndicator(
            refreshing = false,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
                .padding(top = contentPadding.calculateTopPadding())
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
            LocationWeatherInfo(
                location = location,
                weather = weather
            )
        }
    }
}

@Composable
private fun LocationWeatherInfo(
    location: Location,
    weather: WeatherResult?
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
