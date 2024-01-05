package data

import android.content.Context
import androidx.startup.Initializer

internal object ContextHolder {
    lateinit var context: Context
}

internal class ContextInitializer : Initializer<Context> {
    override fun create(context: Context): Context = context.applicationContext.also { ContextHolder.context = it }
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}