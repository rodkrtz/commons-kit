package com.rodkrtz.commonskit.domain.shared.region

import com.rodkrtz.commonskit.core.round
import java.math.RoundingMode

public data class LocationPoint(
    val address: String,
    var latitude: Double,
    var longitude: Double
) {
    init {
        require(latitude in -90.0..90.0) { "Invalid Latitude: $latitude" }
        require(longitude in -180.0..180.0) { "Invalid Longitude: $longitude" }
    }

    public fun rounded(decimals: Int, mode: RoundingMode = RoundingMode.CEILING): LocationPoint {
        return this.copy(
            latitude = this.latitude.round(decimals, mode),
            longitude = this.longitude.round(decimals, mode)
        )
    }

}