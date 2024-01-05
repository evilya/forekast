package data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import me.evko.forekast.BuildKonfig

class WeatherApi {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.BODY
        }
        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = "api.weatherapi.com"
                parameters.append("key", BuildKonfig.API_KEY)
                // fixme remove workaround, see:
                // https://youtrack.jetbrains.com/issue/KTOR-730/Cant-set-a-base-url-that-includes-path-data
                encodedPath = "/v1/$encodedPath"
            }
            header("accept", "application/json")
        }
    }

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
