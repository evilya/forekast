package ui

import data.WeatherConditionCode
import forekast.generated.resources.Res
import org.jetbrains.compose.resources.DrawableResource

val WeatherConditionCode.icon: DrawableResource
    get() = with(Res.drawable) {
        when (this@icon) {
            WeatherConditionCode.SUNNY -> clear_day
            WeatherConditionCode.PARTLY_CLOUDY -> partly_cloudy_day
            WeatherConditionCode.CLOUDY -> cloudy
            WeatherConditionCode.OVERCAST -> cloudy
            WeatherConditionCode.MIST -> fog
            WeatherConditionCode.FOG -> fog
            WeatherConditionCode.FREEZING_FOG -> fog
            WeatherConditionCode.PATCHY_SLEET_POSSIBLE -> sleet
            WeatherConditionCode.PATCHY_FREEZING_DRIZZLE_POSSIBLE -> sleet
            WeatherConditionCode.FREEZING_DRIZZLE -> sleet
            WeatherConditionCode.HEAVY_FREEZING_DRIZZLE -> sleet
            WeatherConditionCode.MODERATE_OR_HEAVY_FREEZING_RAIN -> sleet
            WeatherConditionCode.LIGHT_SLEET -> sleet
            WeatherConditionCode.MODERATE_OR_HEAVY_SLEET -> sleet
            WeatherConditionCode.PATCHY_RAIN_POSSIBLE -> rain
            WeatherConditionCode.PATCHY_LIGHT_DRIZZLE -> rain
            WeatherConditionCode.LIGHT_DRIZZLE -> rain
            WeatherConditionCode.PATCHY_LIGHT_RAIN -> rain
            WeatherConditionCode.LIGHT_RAIN -> rain
            WeatherConditionCode.MODERATE_RAIN_AT_TIMES -> rain
            WeatherConditionCode.MODERATE_RAIN -> rain
            WeatherConditionCode.PATCHY_SNOW_POSSIBLE -> snow
            WeatherConditionCode.BLOWING_SNOW -> snow
            WeatherConditionCode.BLIZZARD -> snow
            WeatherConditionCode.PATCHY_LIGHT_SNOW -> snow
            WeatherConditionCode.LIGHT_SNOW -> snow
            WeatherConditionCode.PATCHY_MODERATE_SNOW -> snow
            WeatherConditionCode.MODERATE_SNOW -> snow
            WeatherConditionCode.PATCHY_HEAVY_SNOW -> snow_showers_day
            WeatherConditionCode.HEAVY_SNOW -> snow_showers_day
            WeatherConditionCode.ICE_PELLETS -> snow
            WeatherConditionCode.HEAVY_RAIN -> showers_day
            WeatherConditionCode.HEAVY_RAIN_AT_TIMES -> showers_day
            WeatherConditionCode.LIGHT_RAIN_SHOWER -> showers_day
            WeatherConditionCode.MODERATE_OR_HEAVY_RAIN_SHOWER -> showers_day
            WeatherConditionCode.TORRENTIAL_RAIN_SHOWER -> showers_day
            WeatherConditionCode.LIGHT_SLEET_SHOWERS -> showers_day
            WeatherConditionCode.MODERATE_OR_HEAVY_SLEET_SHOWERS -> showers_day
            WeatherConditionCode.LIGHT_SNOW_SHOWERS -> snow_showers_day
            WeatherConditionCode.MODERATE_OR_HEAVY_SNOW_SHOWERS -> snow_showers_day
            WeatherConditionCode.LIGHT_FREEZING_RAIN -> rain_snow_showers_day
            WeatherConditionCode.LIGHT_SHOWERS_OF_ICE_PELLETS -> rain_snow_showers_day
            WeatherConditionCode.MODERATE_OR_HEAVY_SHOWERS_OF_ICE_PELLETS -> rain_snow_showers_day
            WeatherConditionCode.THUNDERY_OUTBREAKS_POSSIBLE -> thunder
            WeatherConditionCode.PATCHY_LIGHT_RAIN_WITH_THUNDER -> thunder_rain
            WeatherConditionCode.MODERATE_OR_HEAVY_RAIN_WITH_THUNDER -> thunder_showers_day
            WeatherConditionCode.PATCHY_LIGHT_SNOW_WITH_THUNDER -> snow
            WeatherConditionCode.MODERATE_OR_HEAVY_SNOW_WITH_THUNDER -> snow_showers_day
        }
    }