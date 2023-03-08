package data

import android.content.Context
import androidx.startup.Initializer
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings

private lateinit var appContext: Context

actual fun createSettings(): ObservableSettings {
    return SharedPreferencesSettings.Factory(appContext).create()
}

internal class SettingsInitializer : Initializer<Context> {
    override fun create(context: Context): Context = context.applicationContext.also { appContext = it }
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}