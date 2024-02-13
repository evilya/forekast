package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import data.LocationRepository


class WeatherDetailsScreenModel(private val locationRepository: LocationRepository) : ScreenModel {

}

class WeatherDetailsScreen(private val locationId: Long) : Screen {

    @Composable
    override fun Content() {
        val locationRepository = LocalLocationRepositoryProvider.current
        val screenModel = rememberScreenModel { WeatherDetailsScreenModel(locationRepository) }
        val navigator = LocalNavigator.currentOrThrow

        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Text("$locationId")
                Button(onClick = navigator::pop) {
                    Text("Back")
                }
            }
        }
    }
}