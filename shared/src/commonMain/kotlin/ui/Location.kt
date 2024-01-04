@file:OptIn(FlowPreview::class)

package ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import data.Location
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter

@ExperimentalMaterial3Api
@Composable
fun AddLocation(
    onLocationAdded: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    val locationName = remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        TextField(
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words,
                autoCorrect = false,
                imeAction = ImeAction.Done,
            ),
            placeholder = { Text(text = "Search") },
            singleLine = true,
            value = locationName.value,
            onValueChange = { locationName.value = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
        )

        var searchInProgress by remember { mutableStateOf(false) }
        var locations by remember { mutableStateOf(emptyList<Location>()) }
        val weatherApi = LocalWeatherApi.current
        
        LaunchedEffect(Unit) {
            snapshotFlow { locationName.value }
                .filter { it.isNotBlank() }
                .debounce(500)
                .collectLatest {
                    searchInProgress = true
                    weatherApi.searchLocation(it)
                        .onSuccess { locations = it }
                    searchInProgress = false
                }
        }

        if (searchInProgress) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }

        LazyColumn(
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()
        ) {
            locations.map { location ->
                item(key = location.id) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLocationAdded(location) }
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = location.name,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = location.country ?: "",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            }
        }
    }
}