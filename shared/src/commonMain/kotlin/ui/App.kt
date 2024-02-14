package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import di.commonModule
import org.koin.compose.KoinApplication
import ui.theme.AppTheme

@Composable
fun App() {
    KoinApplication(
        application = { modules(commonModule) }
    ) {
        val useDarkTheme by remember { mutableStateOf(false) }
        AppTheme(useDarkTheme) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .safeDrawingPadding()
            ) {
                Navigator(CurrentWeatherScreen())
            }
        }
    }
}
