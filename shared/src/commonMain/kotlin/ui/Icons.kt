package ui

import data.WeatherConditionCode
import data.WeatherConditionCode.*
import forekast.shared.generated.resources.*
import forekast.shared.generated.resources.Res
import org.jetbrains.compose.resources.DrawableResource

val WeatherConditionCode.icon: DrawableResource
    get() =
        with(Res.drawable) {
            when (this@icon) {
                SUNNY -> clear_day
                PARTLY_CLOUDY -> partly_cloudy_day
                CLOUDY -> cloudy
                OVERCAST -> cloudy
                MIST -> fog
                FOG -> fog
                PATCHY_SLEET_POSSIBLE -> sleet
                FREEZING_FOG -> fog
                PATCHY_FREEZING_DRIZZLE_POSSIBLE -> sleet
                FREEZING_DRIZZLE -> sleet
                HEAVY_FREEZING_DRIZZLE -> sleet
                MODERATE_OR_HEAVY_FREEZING_RAIN -> sleet
                LIGHT_SLEET -> sleet
                MODERATE_OR_HEAVY_SLEET -> sleet
                PATCHY_RAIN_POSSIBLE -> rain
                PATCHY_LIGHT_DRIZZLE -> rain
                LIGHT_DRIZZLE -> rain
                PATCHY_LIGHT_RAIN -> rain
                LIGHT_RAIN -> rain
                MODERATE_RAIN_AT_TIMES -> rain
                MODERATE_RAIN -> rain
                PATCHY_SNOW_POSSIBLE -> snow
                BLOWING_SNOW -> snow
                BLIZZARD -> snow
                PATCHY_LIGHT_SNOW -> snow
                LIGHT_SNOW -> snow
                PATCHY_MODERATE_SNOW -> snow
                MODERATE_SNOW -> snow
                PATCHY_HEAVY_SNOW -> snow_showers_day
                HEAVY_SNOW -> snow_showers_day
                ICE_PELLETS -> snow
                HEAVY_RAIN -> showers_day
                HEAVY_RAIN_AT_TIMES -> showers_day
                LIGHT_RAIN_SHOWER -> showers_day
                MODERATE_OR_HEAVY_RAIN_SHOWER -> showers_day
                TORRENTIAL_RAIN_SHOWER -> showers_day
                LIGHT_SLEET_SHOWERS -> showers_day
                MODERATE_OR_HEAVY_SLEET_SHOWERS -> showers_day
                LIGHT_SNOW_SHOWERS -> snow_showers_day
                MODERATE_OR_HEAVY_SNOW_SHOWERS -> snow_showers_day
                LIGHT_FREEZING_RAIN -> rain_snow_showers_day
                LIGHT_SHOWERS_OF_ICE_PELLETS -> rain_snow_showers_day
                MODERATE_OR_HEAVY_SHOWERS_OF_ICE_PELLETS -> rain_snow_showers_day
                THUNDERY_OUTBREAKS_POSSIBLE -> thunder
                PATCHY_LIGHT_RAIN_WITH_THUNDER -> thunder_rain
                MODERATE_OR_HEAVY_RAIN_WITH_THUNDER -> thunder_showers_day
                PATCHY_LIGHT_SNOW_WITH_THUNDER -> snow
                MODERATE_OR_HEAVY_SNOW_WITH_THUNDER -> snow_showers_day
            }
        }
