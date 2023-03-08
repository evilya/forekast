package data

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow
import utilities.decodeValue
import utilities.encodeValue
import utilities.getDecodeValueFlow

@OptIn(ExperimentalSettingsApi::class)
class LocationRepository {

    private val settings = createSettings()
    private val flowSettings = settings.toFlowSettings()

    var locations: List<Location>
        get() = settings.decodeValue(LOCATIONS_KEY, emptyList())
        set(value) = settings.encodeValue(LOCATIONS_KEY, value)

    var selectedLocation: Location?
        get() = settings.decodeValue(SELECTED_LOCATION_KEY, null)
        set(value) {
            if (value == null) settings.remove(SELECTED_LOCATION_KEY)
            else settings.encodeValue(SELECTED_LOCATION_KEY, value)
        }

    fun addLocation(location: Location) {
        locations = locations + location
    }

    fun removeLocation(location: Location) {
        locations = locations - location
    }

    fun observeLocations(): Flow<List<Location>> {
        return flowSettings.getDecodeValueFlow<List<Location>>(LOCATIONS_KEY, emptyList())
    }

    fun observeSelectedLocation(): Flow<Location?> {
        return flowSettings.getDecodeValueFlow<Location?>(SELECTED_LOCATION_KEY, null)
    }

    private companion object {
        const val LOCATIONS_KEY = "LOCATIONS"
        const val SELECTED_LOCATION_KEY = "SELECTED_LOCATION"
    }
}
