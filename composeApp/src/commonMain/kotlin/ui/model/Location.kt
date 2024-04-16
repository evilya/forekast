package ui.model

import data.LocationId

data class Location(
    val id: LocationId,
    val name: String,
    val country: String?,
    val added: Boolean,
) {

    fun toLocation(): data.Location {
        return data.Location(this.id, this.name, null, this.country)
    }
}

fun data.Location.toLocation(added: Boolean): Location {
    return Location(this.id, this.name, this.country, added)
}
