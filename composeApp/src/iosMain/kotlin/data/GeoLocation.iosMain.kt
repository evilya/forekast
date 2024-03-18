package data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLLocationAccuracyKilometer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class)
actual suspend fun getCurrentLocation(): GeoLocation = suspendCancellableCoroutine { continuation ->
    val locationManager = CLLocationManager().apply {
        requestWhenInUseAuthorization()
        desiredAccuracy = kCLLocationAccuracyKilometer
        startUpdatingLocation()
    }

    val location = locationManager.location
    if (location != null) {
        continuation.resume(location.coordinate().useContents { GeoLocation(latitude, longitude) })
    } else {
        continuation.resumeWithException(IllegalStateException("Failed to retrieve location"))
    }

    continuation.invokeOnCancellation {
        locationManager.stopUpdatingLocation()
    }
}
