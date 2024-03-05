package data

import ui.WeatherResult

class WeatherRepository(private val api: WeatherApi) {
    private val cache = mutableMapOf<LocationId, WeatherResult>()

    suspend fun getCurrentWeather(locationId: LocationId): WeatherResult {
        return cache.getOrPut(locationId) {
            api.getCurrentWeather(locationId)
        }
    }

    fun clearCache() {
        cache.clear()
    }
}
