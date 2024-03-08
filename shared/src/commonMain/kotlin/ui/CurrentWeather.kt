package ui

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import data.WeatherData
import data.WeatherRepository
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
import ui.utils.BottomSheetNavigator

typealias WeatherResult = Result<WeatherData>

class CurrentWeatherScreenModel(
    private val locationRepository: LocationRepository,
) : ScreenModel {
    val locations = locationRepository.observeLocations()

    fun removeLocation(location: Location) {
        locationRepository.removeLocation(location)
    }
}

class CurrentWeatherScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<CurrentWeatherScreenModel>()
        val locations by screenModel.locations.collectAsState(emptyList())
        val navigator = LocalNavigator.currentOrThrow

        BottomSheetNavigator(
            skipPartiallyExpanded = true,
            windowInsets = WindowInsets.ime,
        ) { bottomSheetNavigator ->
            CurrentWeather(
                locations = locations,
                onLocationAdd = {
                    bottomSheetNavigator.show(AddLocationScreen())
                },
                onLocationClick = { location ->
                    navigator.push(WeatherDetailsScreen(location))
                },
                onLocationDelete = { location ->
                    screenModel.removeLocation(location)
                },
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
                        contentDescription = "Add location",
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { contentPadding ->
        LocationsList(
            locations = locations,
            onLocationClick = onLocationClick,
            onLocationAdd = onLocationAdd,
            onLocationDelete = onLocationDelete,
            contentPadding = contentPadding,
        )
    }
}

@Composable
private fun LocationsList(
    locations: List<Location>,
    onLocationClick: (Location) -> Unit,
    onLocationAdd: () -> Unit,
    onLocationDelete: (Location) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val weatherRepository = koinInject<WeatherRepository>()
    var reloadTrigger by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = false,
        onRefresh = {
            weatherRepository.clearCache()
            reloadTrigger = !reloadTrigger
        },
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState),
    ) {
        val itemsState by updateAnimatedItemsState(locations)

        if (locations.isNotEmpty() || itemsState.any { !it.visibility.isIdle }) {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = contentPadding,
            ) {
                animatedItemsIndexed(
                    state = itemsState,
                    key = { it.id.id },
                    enterTransition = slideInHorizontally(initialOffsetX = { -it }),
                    exitTransition = slideOutHorizontally(targetOffsetX = { -it }),
                ) { _, location ->
                    var weather by remember { mutableStateOf<WeatherResult?>(null) }
                    LaunchedEffect(reloadTrigger) {
                        weather = weatherRepository.getCurrentWeather(location.id)
                    }
                    LocationWeatherCard(
                        location = location,
                        weather = weather,
                        onClick = { onLocationClick(location) },
                        onDelete = { onLocationDelete(location) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .animateItemPlacement(),
                    )
                }
            }
        } else {
            EmptyLocations(
                onAddLocationClick = onLocationAdd,
                modifier = Modifier.fillMaxSize(),
            )
        }

        PullRefreshIndicator(
            refreshing = false,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = contentPadding.calculateTopPadding()),
        )
    }
}

@Composable
private fun EmptyLocations(
    onAddLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Image(
                imageVector = vectorResource(Res.drawable.earth),
                contentDescription = "Weather icon",
                modifier = Modifier.size(100.dp),
            )
            OutlinedButton(
                onClick = onAddLocationClick,
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
    modifier: Modifier = Modifier,
) {
    DragToDelete(
        modifier = modifier,
        shape = CardDefaults.shape,
        onValueChanged = {
            if (it == End) onDelete()
            true
        },
    ) {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            onClick = onClick,
        ) {
            LocationWeatherInfo(
                location = location,
                weather = weather,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun LocationWeatherInfo(
    location: Location,
    weather: WeatherResult?,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier,
    ) {
        val marqueeModifier = Modifier.basicMarquee(
            iterations = Int.MAX_VALUE,
            delayMillis = 0,
            spacing = MarqueeSpacing(8.dp),
            velocity = 60.dp,
        )
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = marqueeModifier,
            )
            Text(
                text = weather?.getOrNull()?.current?.weatherCondition?.text ?: "",
                style = MaterialTheme.typography.headlineSmall,
                modifier = marqueeModifier,
            )
        }
        weather?.onSuccess { weather ->
            weather.current.weatherCondition.code?.icon?.let { weatherIcon ->
                Image(
                    imageVector = vectorResource(weatherIcon),
                    contentDescription = "Weather icon",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(60.dp).weight(1f),
                )
            }
            val temperatureUnit = stringResource(Res.string.unit_celsius)
            Text(
                text = "${weather.current.temperature.toInt()}$temperatureUnit",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.End,
                modifier = Modifier.width(80.dp).weight(1f),
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
