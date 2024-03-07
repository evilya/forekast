package di

import data.LocationRepository
import data.WeatherApi
import data.WeatherRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.URLProtocol
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.evko.forekast.BuildConfig
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ui.AddLocationBottomSheetScreenModel
import ui.CurrentWeatherScreenModel
import ui.WeatherDetailsScreenModel

val commonModule = module {
    single { createJson() }
    single { createKtorClient(get()) }
    singleOf(::WeatherApi)
    singleOf(::LocationRepository)
    singleOf(::WeatherRepository)

    factoryOf(::CurrentWeatherScreenModel)
    factoryOf(::WeatherDetailsScreenModel)
    factoryOf(::AddLocationBottomSheetScreenModel)
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
            parameters.append(
                "key",
                BuildConfig.API_KEY,
            )
            // fixme remove workaround, see:
            // https://youtrack.jetbrains.com/issue/KTOR-730/Cant-set-a-base-url-that-includes-path-data
            encodedPath = "/v1/$encodedPath"
        }
        header("accept", "application/json")
    }
}
