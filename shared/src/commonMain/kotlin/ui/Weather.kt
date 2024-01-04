@file:OptIn(
    ExperimentalResourceApi::class, ExperimentalMaterialApi::class, ExperimentalFoundationApi::class
)

package ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.Location
import data.LocationWeather
import data.WeatherData
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@Composable
fun CurrentWeatherScreen(
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

        FloatingActionButton(
            onClick = onAddLocationClick,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add location"
            )
        }
    }
}

@Composable
private fun AddLocationButton(onAddLocationClick: () -> Unit) {
    OutlinedButton(
        onClick = onAddLocationClick,
        modifier = Modifier.padding(16.dp).height(48.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add location"
        )
        Text(text = "Add location")
    }
}

val icons = listOf("ic_cloud.xml", "ic_rain.xml", "ic_sun.xml", "ic_sun_and_cloud.xml")

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
            Column(modifier = Modifier.weight(2f)) {
                Text(text = city.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = weather.getOrNull()?.current?.weatherCondition?.text ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE,
                        delayMillis = 0,
                        spacing = MarqueeSpacing(8.dp),
                        velocity = 60.dp
                    )
                )
            }
            weather.onSuccess { weather ->
                if (weather != null) {
                    Image(
                        painter = painterResource(DrawableResource(icons.random())),
                        contentDescription = "Weather icon",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(60.dp).weight(1f)
                    )
                    Text(
                        text = "${weather.current.temperature.toInt()}°C",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(80.dp).weight(1f)
                    )
                }
            }.onFailure {
                Image(
                    painter = painterResource(DrawableResource("ic_error.xml")),
                    contentDescription = "Error icon",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(80.dp),
                )
            }
        }
    }
}