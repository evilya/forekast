package data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.path
import utilities.runSuspendCatching

class WeatherApi(private val client: HttpClient) {
    suspend fun getCurrentWeather(locationId: LocationId): Result<WeatherData> {
        return runSuspendCatching {
            client.get {
                url {
                    path("current.json")
                    parameter("q", "id:${locationId.id}")
                }
            }.body<WeatherData>()
        }
    }

    suspend fun searchLocation(query: String): Result<List<Location>> {
        return runSuspendCatching {
            client.get {
                url {
                    path("search.json")
                    parameter("q", query)
                }
            }.body<List<Location>>()
        }
    }

    suspend fun searchLocation(location: GeoLocation): Result<Location?> {
        return runSuspendCatching {
            client.get {
                url {
                    path("search.json")
                    parameter("q", with(location) { "$latitude,$longitude" })
                }
            }.body<List<Location>>().firstOrNull()
        }
    }
}
