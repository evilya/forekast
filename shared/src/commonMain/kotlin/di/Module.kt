package di

import data.LocationRepository
import data.WeatherApi
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import me.evko.forekast.BuildConfig
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ui.CurrentWeatherScreenModel
import ui.WeatherDetailsScreenModel

val commonModule = module {
    single { createJson() }
    single { createKtorClient(get()) }
    singleOf(::WeatherApi)
    singleOf(::LocationRepository)

    factory { CurrentWeatherScreenModel(get()) }
    factory { WeatherDetailsScreenModel(get()) }
}

fun createJson(): Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

fun createKtorClient(json: Json): HttpClient = HttpClient {
    install(ContentNegotiation) { json(json) }
    install(Logging) {
        level = LogLevel.BODY
    }
    defaultRequest {
        url {
            protocol = URLProtocol.HTTPS
            host = "api.weatherapi.com"
            parameters.append("key", BuildConfig.API_KEY)
            // fixme remove workaround, see:
            // https://youtrack.jetbrains.com/issue/KTOR-730/Cant-set-a-base-url-that-includes-path-data
            encodedPath = "/v1/$encodedPath"
        }
        header("accept", "application/json")
    }
}
