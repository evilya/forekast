package data

data class LocationWeather(
    val location: Location,
    val weather: Result<WeatherData?>
)