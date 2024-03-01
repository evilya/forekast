package data

import ui.WeatherResult

class WeatherRepository(private val api: WeatherApi) {
    private val cache = mutableMapOf<Location, WeatherResult>()

    suspend fun getCurrentWeather(location: Location): WeatherResult {
        return cache.getOrPut(location) {
            api.getCurrentWeather(location)
        }
    }

    fun clearCache() {
        cache.clear()
    }
}