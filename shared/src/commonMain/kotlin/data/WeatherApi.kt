package data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class WeatherApi(private val client: HttpClient) {

    suspend fun getCurrentWeather(location: Location): Result<WeatherData> {
        return runCatching {
            client.get {
                url {
                    path("current.json")
                    parameter("q", location.name)
                }
            }.body<WeatherData>()
        }
    }

    suspend fun searchLocation(query: String): Result<List<Location>> {
        return runCatching {
            client.get {
                url {
                    path("search.json")
                    parameter("q", query)
                }
            }.body<List<Location>>()
        }
    }

    suspend fun searchLocation(location: GeoLocation): Result<Location?> {
        return runCatching {
            client.get {
                url {
                    path("search.json")
                    parameter("q", with(location) { "$latitude,$longitude" })
                }
            }.body<List<Location>>().firstOrNull()
        }
    }
}
