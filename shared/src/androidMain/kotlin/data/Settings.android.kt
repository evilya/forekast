package data

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings

actual fun createSettings(): ObservableSettings {
    return SharedPreferencesSettings.Factory(ContextHolder.context).create()
}