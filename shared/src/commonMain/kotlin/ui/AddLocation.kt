package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import data.Location
import data.LocationId
import data.LocationRepository
import data.WeatherApi
import data.getCurrentLocation
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import forekast.shared.generated.resources.Res
import forekast.shared.generated.resources.location_search_current
import forekast.shared.generated.resources.location_search_hint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

class AddLocationBottomSheetScreenModel(
    private val locationRepository: LocationRepository,
) : ScreenModel {
    val locations = locationRepository.observeLocations()

    fun addLocation(location: Location) {
        locationRepository.addLocation(location)
    }
}

class AddLocationBottomSheetScreen(
    private val onDismiss: () -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<AddLocationBottomSheetScreenModel>()
        val locations by screenModel.locations.collectAsState(emptyList())

        AddLocationBottomSheet(
            locations = locations,
            onLocationAdded = screenModel::addLocation,
            onDismiss = onDismiss,
        )
    }
}

@Composable
fun AddLocationBottomSheet(
    locations: List<Location>,
    onLocationAdded: (Location) -> Unit,
    onDismiss: () -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = onDismiss,
        windowInsets = WindowInsets.ime,
    ) {
        AddLocation(
            addedLocationIds = locations.map { it.id }.toSet(),
            onLocationAdded = {
                if (locations.contains(it)) return@AddLocation
                onLocationAdded(it)
                keyboardController?.hide()
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )
        LaunchedEffect(bottomSheetState.targetValue) {
            if (bottomSheetState.targetValue == SheetValue.Expanded) focusRequester.requestFocus()
        }
    }
}

@Composable
fun AddLocation(
    addedLocationIds: Set<LocationId>,
    onLocationAdded: (Location) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locationName = remember { mutableStateOf("") }
    var searchInProgress by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf(emptyList<Location>()) }
    var currentLocationAdded by remember { mutableStateOf(false) }
    val weatherApi = koinInject<WeatherApi>()
    val coroutineScope = rememberCoroutineScope()
    val permissionControllerFactory = rememberPermissionsControllerFactory()
    val permissionController = remember(permissionControllerFactory) {
        permissionControllerFactory.createPermissionsController()
    }

    BindEffect(permissionController)

    @OptIn(FlowPreview::class)
    LaunchedEffect(locationName) {
        snapshotFlow { locationName.value }
            .filter { it.isNotBlank() }
            .debounce(500)
            .collectLatest {
                searchInProgress = true
                weatherApi.searchLocation(it)
                    .onSuccess { locations -> searchResults = locations }
                searchInProgress = false
            }
    }

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
                value = locationName.value,
                onValueChange = { locationName.value = it },
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
                onClick = {
                    coroutineScope.launch {
                        val currentLocation =
                            runCatching {
                                permissionController.providePermission(Permission.LOCATION)
                                getCurrentLocation()
                            }.getOrNull() ?: return@launch

                        searchInProgress = true
                        weatherApi.searchLocation(currentLocation)
                            .onSuccess { location ->
                                location?.let(onLocationAdded)
                                    ?.also { currentLocationAdded = true }
                            }
                        searchInProgress = false
                    }
                },
            )
        }
        searchResults.map { location ->
            item(key = location.id.id) {
                SearchLocationItem(
                    location = location,
                    added = addedLocationIds.contains(location.id),
                    onClick = { location -> location?.let(onLocationAdded) },
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
    onClick: (Location?) -> Unit,
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
