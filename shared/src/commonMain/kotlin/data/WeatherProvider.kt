package data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import me.evko.forekast.BuildKonfig

class WeatherProvider {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        defaultRequest {
            url("https://weatherapi-com.p.rapidapi.com")

            header("X-RapidAPI-Key", BuildKonfig.API_KEY)
            header("X-RapidAPI-Host", "weatherapi-com.p.rapidapi.com")
        }
    }

    suspend fun getWeather(location: Location, days: Int = 3): Result<WeatherData> {
        return runCatching {
            client.get {
                url {
                    path("forecast.json")
                    parameter("q", location.name)
                    parameter("days", days)
                }
            }.body<WeatherData>()
        }
    }
}

