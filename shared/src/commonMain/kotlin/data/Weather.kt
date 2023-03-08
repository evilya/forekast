package data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    @SerialName("current") val current: CurrentWeather
)

@Serializable
data class CurrentWeather(
    @SerialName("temp_c") val temperature: Double,
    @SerialName("condition") val weatherCondition: WeatherCondition
)

@Serializable
data class WeatherCondition(
    @SerialName("text") val text: String,
)
