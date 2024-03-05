package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import data.LocationId
import data.WeatherRepository
import forekast.shared.generated.resources.Res
import forekast.shared.generated.resources.ic_error
import forekast.shared.generated.resources.unit_celsius
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

class WeatherDetailsScreenModel(private val weatherRepository: WeatherRepository) : ScreenModel {
    suspend fun getCurrentWeather(locationId: LocationId): WeatherResult {
        return weatherRepository.getCurrentWeather(locationId)
    }
}

class WeatherDetailsScreen(private val location: Location) : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<WeatherDetailsScreenModel>()
        val navigator = LocalNavigator.currentOrThrow

        var weather by remember { mutableStateOf<WeatherResult?>(null) }
        LaunchedEffect(Unit) {
            weather = screenModel.getCurrentWeather(location.id)
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = location.name) },
                    navigationIcon = {
                        IconButton(onClick = navigator::pop) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
            },
        ) {
            LocationWeatherInfo(
                location = location,
                weather = weather,
                modifier = Modifier.fillMaxSize(),
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Text(
            text = location.name,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        weather?.onSuccess { weather ->
            weather.current.weatherCondition.code?.icon?.let { weatherIcon ->
                Image(
                    imageVector = vectorResource(weatherIcon),
                    contentDescription = "Weather icon",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(200.dp),
                )
            }
            val temperatureUnit = stringResource(Res.string.unit_celsius)
            Text(
                text = "${weather.current.temperature.toInt()}$temperatureUnit",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )
        }?.onFailure {
            Image(
                imageVector = vectorResource(Res.drawable.ic_error),
                contentDescription = "Error icon",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(80.dp),
            )
        }
        Text(
            text = weather?.getOrNull()?.current?.weatherCondition?.text ?: "",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
