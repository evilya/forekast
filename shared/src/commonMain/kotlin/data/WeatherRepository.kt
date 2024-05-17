package data

import co.touchlab.stately.collections.ConcurrentMutableMap
import ui.screen.WeatherResult

class WeatherRepository(private val api: WeatherApi) {
    private val cache = ConcurrentMutableMap<LocationId, WeatherResult>()

    suspend fun getCurrentWeather(locationId: LocationId): WeatherResult {
        return cache.getOrPut(locationId) {
            api.getCurrentWeather(locationId)
        }
    }

    fun clearCache() {
        cache.clear()
    }
}
