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

    private var locations: List<Location>
        get() = settings.decodeValue(LOCATIONS_KEY, emptyList())
        set(value) = settings.encodeValue(LOCATIONS_KEY, value)

    fun addLocation(location: Location) {
        locations += location
    }

    fun removeLocation(location: Location) {
        locations -= location
    }

    fun observeLocations(): Flow<List<Location>> {
        return flowSettings.getDecodeValueFlow<List<Location>>(LOCATIONS_KEY, emptyList())
    }

    private companion object {
        const val LOCATIONS_KEY = "LOCATIONS"
    }
}
