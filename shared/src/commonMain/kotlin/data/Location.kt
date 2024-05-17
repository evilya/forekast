package data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Location(
    @SerialName("id") val id: LocationId,
    @SerialName("name") val name: String,
    @SerialName("region") val region: String? = null,
    @SerialName("country") val country: String? = null,
)

@JvmInline
@Serializable
value class LocationId(val id: Long)
