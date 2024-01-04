package data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Location(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("region") val region: String? = null,
    @SerialName("country") val country: String? = null,
)