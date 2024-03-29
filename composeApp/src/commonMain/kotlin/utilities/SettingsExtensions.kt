package utilities

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSettingsApi::class)
inline fun <reified T> FlowSettings.getDecodeValueFlow(
    key: String,
    defaultValue: T,
): Flow<T> = getStringOrNullFlow(key).map {
    it?.let {
        Json.decodeFromString<T>(it)
    } ?: defaultValue
}

inline fun <reified T> Settings.encodeValue(
    key: String,
    value: T,
) = set(
    key,
    Json.encodeToString(value),
)

inline fun <reified T> Settings.decodeValue(
    key: String,
    defaultValue: T,
): T {
    return getStringOrNull(key)?.let {
        Json.decodeFromString<T>(it)
    } ?: defaultValue
}
