package data

import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

actual suspend fun getCurrentLocation(): GeoLocation {
    val location = LocationServices.getFusedLocationProviderClient(ContextHolder.context)
        .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
        .await()
    return GeoLocation(location.latitude, location.longitude)
}
