package data

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings

actual fun createSettings(): ObservableSettings {
    return NSUserDefaultsSettings.Factory().create()
}