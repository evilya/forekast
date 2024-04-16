package ui.screen

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import data.LocationRepository
import data.getCurrentLocation
import dev.icerock.moko.permissions.*
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import forekast.composeapp.generated.resources.Res
import forekast.composeapp.generated.resources.location_search_current
import forekast.composeapp.generated.resources.location_search_hint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.core.parameter.parametersOf
import ui.model.Location
import ui.model.toLocation

class AddLocationScreenModel(
    private val locationRepository: LocationRepository,
    val permissionController: PermissionsController,
) : ScreenModel {
    private val addedLocations = locationRepository.observeLocations()
        .map { locations -> locations.map { it.id }.toSet() }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptySet(),
        )

    private val locationSearchQueryState = mutableStateOf("")
    val locationSearchQuery: String by locationSearchQueryState

    private val locationSearchInProgress = MutableStateFlow(false)

    private val currentLocationAdded = MutableStateFlow(false)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults = snapshotFlow { locationSearchQueryState.value }
        .filter { it.isNotBlank() }
        .debounce(500)
        .mapLatest { query ->
            locationSearchInProgress.value = true
            locationRepository.searchLocation(query)
                .also {
                    locationSearchInProgress.value = false
                }
        }
        .combine(addedLocations) { searchResult, addedLocations ->
            searchResult.map { locations ->
                locations.map { it.toLocation(it.id in addedLocations) }
            }
        }

    val screenState = combine(
        locationSearchInProgress,
        searchResults,
        currentLocationAdded,
    ) { locationSearchInProgress, searchResult, currentLocationAdded ->
        AddLocationState(locationSearchInProgress, searchResult, currentLocationAdded)
    }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AddLocationState(),
        )

    fun onLocationQueryChange(query: String) {
        locationSearchQueryState.value = query
    }

    fun addLocation(location: Location) {
        // todo check if already added
        locationRepository.addLocation(location.toLocation())
    }

    fun addCurrentLocation() {
        if (currentLocationAdded.value) return
        screenModelScope.launch {
            try {
                permissionController.providePermission(Permission.LOCATION)
                val currentLocation = getCurrentLocation()

                locationRepository.searchLocation(currentLocation)
                    .onSuccess { location ->
                        location?.let(locationRepository::addLocation).also {
                            currentLocationAdded.value = true
                        }
                    }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                return@launch
            }
        }
    }
}

data class AddLocationState(
    val searchInProgress: Boolean = false,
    val searchResults: Result<List<Location>> = Result.success(emptyList()),
    val currentLocationAdded: Boolean = false,
)

class AddLocationScreen : Screen {
    @Composable
    override fun Content() {
        val permissionControllerFactory = rememberPermissionsControllerFactory()
        val screenModel = getScreenModel<AddLocationScreenModel>(
            parameters = { parametersOf(permissionControllerFactory.createPermissionsController()) },
        )
        val screenState by screenModel.screenState.collectAsState()

        val focusRequester = remember { FocusRequester() }

        BindEffect(screenModel.permissionController)

        AddLocation(
            locationQuery = screenModel.locationSearchQuery,
            onLocationQueryChange = { screenModel.onLocationQueryChange(it) },
            searchResults = screenState.searchResults,
            searchInProgress = screenState.searchInProgress,
            currentLocationAdded = screenState.currentLocationAdded,
            onSearchLocationClick = { screenModel.addLocation(it) },
            onCurrentLocationClick = { screenModel.addCurrentLocation() },
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
private fun AddLocation(
    locationQuery: String,
    onLocationQueryChange: (String) -> Unit,
    searchResults: Result<List<Location>>,
    searchInProgress: Boolean,
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
                value = locationQuery,
                onValueChange = onLocationQueryChange,
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
    onClick: (Location) -> Unit,
) {
    LocationItem(
        modifier = Modifier
            .clickable(enabled = !location.added) { onClick(location) },
        added = location.added,
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
