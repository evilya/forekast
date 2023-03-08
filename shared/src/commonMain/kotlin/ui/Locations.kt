package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import data.Location

@ExperimentalMaterial3Api
@Composable
fun AddLocation(
    onLocationAdded: (Location) -> Unit,
    modifier: Modifier = Modifier
) {
    val locationName = remember { mutableStateOf("") }

    fun addLocation() {
        if (locationName.value.isNotBlank()) {
            onLocationAdded(Location(locationName.value))
            locationName.value = ""
        }
    }

    Column(
        verticalArrangement = Arrangement.Bottom,
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
            keyboardActions = KeyboardActions(onDone = { addLocation() }),
            placeholder = { Text(text = "Search") },
            singleLine = true,
            value = locationName.value,
            onValueChange = { locationName.value = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
        )
        OutlinedButton(
            onClick = { addLocation() },
            enabled = locationName.value.isNotBlank(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
        ) {
            Text(text = "Add location", style = MaterialTheme.typography.bodyMedium)
        }
    }
}