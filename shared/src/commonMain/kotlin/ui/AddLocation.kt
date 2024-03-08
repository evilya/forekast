package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import data.Location
import data.LocationId
import data.LocationRepository
import data.WeatherApi
import data.getCurrentLocation
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import forekast.shared.generated.resources.Res
import forekast.shared.generated.resources.location_search_current
import forekast.shared.generated.resources.location_search_hint
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.core.parameter.parametersOf

class AddLocationScreenModel(
    private val locationRepository: LocationRepository,
    private val weatherApi: WeatherApi,
    val permissionController: PermissionsController,
) : ScreenModel {
    val addedLocations = locationRepository.observeLocations()
        .map { locations -> locations.map { it.id }.toSet() }

    val locationSearchQuery = mutableStateOf("")
    val locationSearchInProgress = mutableStateOf(false)
    val currentLocationAdded = mutableStateOf(false)

    val searchResults = snapshotFlow { locationSearchQuery.value }
        .filter { it.isNotBlank() }
        .debounce(500)
        .mapLatest { query ->
            locationSearchInProgress.value = true
            weatherApi.searchLocation(query)
                .also { locationSearchInProgress.value = false }
        }

    fun addLocation(location: Location) {
        locationRepository.addLocation(location)
    }

    fun addCurrentLocation() {
        screenModelScope.launch {
            permissionController.providePermission(Permission.LOCATION)
            val currentLocation = runCatching { getCurrentLocation() }
                .getOrNull() ?: return@launch

            weatherApi.searchLocation(currentLocation)
                .onSuccess { location ->
                    location?.let(::addLocation).also {
                        currentLocationAdded.value = true
                    }
                }
        }
    }
}

class AddLocationScreen : Screen {
    @Composable
    override fun Content() {
        val permissionControllerFactory = rememberPermissionsControllerFactory()
        val screenModel = getScreenModel<AddLocationScreenModel>(
            parameters = { parametersOf(permissionControllerFactory.createPermissionsController()) },
        )
        val locationQuery = remember { screenModel.locationSearchQuery }
        val searchResults by screenModel.searchResults.collectAsState(Result.success(emptyList()))
        val searchInProgress by screenModel.locationSearchInProgress
        val addedLocationIds by screenModel.addedLocations.collectAsState(emptySet())
        val currentLocationAdded by screenModel.currentLocationAdded

        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }

        BindEffect(screenModel.permissionController)

        AddLocation(
            locationQuery = locationQuery,
            searchResults = searchResults,
            searchInProgress = searchInProgress,
            addedLocationIds = addedLocationIds,
            currentLocationAdded = currentLocationAdded,
            onSearchLocationClick = {
                if (addedLocationIds.contains(it.id)) return@AddLocation
                screenModel.addLocation(it)
                keyboardController?.hide()
            },
            onCurrentLocationClick = {
                if (currentLocationAdded) return@AddLocation
                screenModel.addCurrentLocation()
                keyboardController?.hide()
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@Composable
fun AddLocation(
    locationQuery: MutableState<String>,
    searchResults: Result<List<Location>>,
    searchInProgress: Boolean,
    addedLocationIds: Set<LocationId>,
    currentLocationAdded: Boolean,
    onSearchLocationClick: (Location) -> Unit,
    onCurrentLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 8.dp).fillMaxWidth(),
    ) {
        item {
            TextField(
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrect = false,
                    imeAction = ImeAction.Done,
                ),
                placeholder = { Text(text = stringResource(Res.string.location_search_hint)) },
                singleLine = true,
                value = locationQuery.value,
                onValueChange = { locationQuery.value = it },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
            )
        }
        if (searchInProgress) {
            item {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
        item {
            CurrentLocationItem(
                added = currentLocationAdded,
                onClick = onCurrentLocationClick,
            )
        }
        searchResults.getOrNull()?.map { location ->
            item(key = location.id.id) {
                SearchLocationItem(
                    location = location,
                    added = addedLocationIds.contains(location.id),
                    onClick = onSearchLocationClick,
                )
            }
        }
    }
}

@Composable
private fun LocationItem(
    modifier: Modifier = Modifier,
    added: Boolean,
    content: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = 8.dp,
                horizontal = 16.dp,
            ),
    ) {
        content()
        Spacer(modifier = Modifier.weight(1f))
        if (added) {
            Icon(
                imageVector = Icons.Sharp.Check,
                contentDescription = "Location added checkmark",
            )
        }
    }
}

@Composable
private fun SearchLocationItem(
    location: Location,
    added: Boolean,
    onClick: (Location) -> Unit,
) {
    LocationItem(
        modifier = Modifier
            .clickable(enabled = !added) { onClick(location) },
        added = added,
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = location.name,
                style = MaterialTheme.typography.titleMedium,
            )
            location.country?.let { country ->
                Text(
                    text = country,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}

@Composable
private fun CurrentLocationItem(
    added: Boolean,
    onClick: () -> Unit,
) {
    LocationItem(
        modifier = Modifier
            .clickable(enabled = !added) { onClick() },
        added = added,
    ) {
        Text(
            text = stringResource(Res.string.location_search_current),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
