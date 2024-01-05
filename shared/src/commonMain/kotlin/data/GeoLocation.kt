package data

data class GeoLocation(val latitude: Double, val longitude: Double)

expect suspend fun getCurrentLocation(): GeoLocation