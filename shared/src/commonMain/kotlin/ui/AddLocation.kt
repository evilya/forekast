package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material3.*
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
import data.Location
import data.WeatherApi
import data.getCurrentLocation
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import forekast.shared.generated.resources.Res
import forekast.shared.generated.resources.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun AddLocationBottomSheet(
    locations: List<Location>,
    onLocationAdded: (Location) -> Unit,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = onDismiss,
        windowInsets = WindowInsets.ime
    ) {
        AddLocation(
            locations = locations,
            onLocationAdded = {
                if (locations.contains(it)) return@AddLocation
                onLocationAdded(it)
                keyboardController?.hide()
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
        LaunchedEffect(bottomSheetState.targetValue) {
            if (bottomSheetState.targetValue == SheetValue.Expanded) focusRequester.requestFocus()
        }
    }
}


@Composable
fun AddLocation(
    locations: List<Location>,
    onLocationAdded: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    val locationName = remember { mutableStateOf("") }
    var searchInProgress by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf(emptyList<Location>()) }
    var currentLocationAdded by remember { mutableStateOf(false) }
    val weatherApi = koinInject<WeatherApi>()
    val coroutineScope = rememberCoroutineScope()
    val permissionControllerFactory = rememberPermissionsControllerFactory()
    val permissionController =
        remember(permissionControllerFactory) { permissionControllerFactory.createPermissionsController() }

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
        modifier = modifier.padding(vertical = 8.dp).fillMaxWidth()
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
            )
        }
        if (searchInProgress) {
            item {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
        item {
            LocationItem(
                isAdded = currentLocationAdded,
                onClick = {
                    coroutineScope.launch {
                        val currentLocation = runCatching {
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
                })
        }
        searchResults.map { location ->
            item(key = location.id.id) {
                LocationItem(
                    location = location,
                    isAdded = locations.map { it.id }.toSet().contains(location.id),
                    onClick = { location -> location?.let(onLocationAdded) }
                )
            }
        }
    }
}

@Composable
private fun LocationItem(
    location: Location? = null,
    isAdded: Boolean,
    onClick: (Location?) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!isAdded) onClick(location) }
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = location?.name ?: stringResource(Res.string.location_search_current),
                style = MaterialTheme.typography.titleMedium,
            )
            location?.country?.let { country ->
                Text(
                    text = country,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        if (isAdded) {
            Icon(
                imageVector = Icons.Sharp.Check,
                contentDescription = "Location added checkmark"
            )
        }
    }
}