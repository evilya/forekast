package data

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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
    @SerialName("code") val code: WeatherConditionCode?,
)

@Serializable(with = WeatherConditionCodeSerializer::class)
enum class WeatherConditionCode(val code: Int) {
    SUNNY(1000),
    PARTLY_CLOUDY(1003),
    CLOUDY(1006),
    OVERCAST(1009),
    MIST(1030),
    PATCHY_RAIN_POSSIBLE(1063),
    PATCHY_SNOW_POSSIBLE(1066),
    PATCHY_SLEET_POSSIBLE(1069),
    PATCHY_FREEZING_DRIZZLE_POSSIBLE(1072),
    THUNDERY_OUTBREAKS_POSSIBLE(1087),
    BLOWING_SNOW(1114),
    BLIZZARD(1117),
    FOG(1135),
    FREEZING_FOG(1147),
    PATCHY_LIGHT_DRIZZLE(1150),
    LIGHT_DRIZZLE(1153),
    FREEZING_DRIZZLE(1168),
    HEAVY_FREEZING_DRIZZLE(1171),
    PATCHY_LIGHT_RAIN(1180),
    LIGHT_RAIN(1183),
    MODERATE_RAIN_AT_TIMES(1186),
    MODERATE_RAIN(1189),
    HEAVY_RAIN_AT_TIMES(1192),
    HEAVY_RAIN(1195),
    LIGHT_FREEZING_RAIN(1198),
    MODERATE_OR_HEAVY_FREEZING_RAIN(1201),
    LIGHT_SLEET(1204),
    MODERATE_OR_HEAVY_SLEET(1207),
    PATCHY_LIGHT_SNOW(1210),
    LIGHT_SNOW(1213),
    PATCHY_MODERATE_SNOW(1216),
    MODERATE_SNOW(1219),
    PATCHY_HEAVY_SNOW(1222),
    HEAVY_SNOW(1225),
    ICE_PELLETS(1237),
    LIGHT_RAIN_SHOWER(1240),
    MODERATE_OR_HEAVY_RAIN_SHOWER(1243),
    TORRENTIAL_RAIN_SHOWER(1246),
    LIGHT_SLEET_SHOWERS(1249),
    MODERATE_OR_HEAVY_SLEET_SHOWERS(1252),
    LIGHT_SNOW_SHOWERS(1255),
    MODERATE_OR_HEAVY_SNOW_SHOWERS(1258),
    LIGHT_SHOWERS_OF_ICE_PELLETS(1261),
    MODERATE_OR_HEAVY_SHOWERS_OF_ICE_PELLETS(1264),
    PATCHY_LIGHT_RAIN_WITH_THUNDER(1273),
    MODERATE_OR_HEAVY_RAIN_WITH_THUNDER(1276),
    PATCHY_LIGHT_SNOW_WITH_THUNDER(1279),
    MODERATE_OR_HEAVY_SNOW_WITH_THUNDER(1282),
}

class WeatherConditionCodeSerializer : KSerializer<WeatherConditionCode?> {
    override fun serialize(encoder: Encoder, value: WeatherConditionCode?) {
        encoder.encodeInt(value?.code ?: 0)
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("weatherConditionCode", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): WeatherConditionCode? {
        val value = decoder.decodeInt()
        return WeatherConditionCode.entries.firstOrNull { it.code == value }
    }
}