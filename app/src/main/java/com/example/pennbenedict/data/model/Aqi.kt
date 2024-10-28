package com.example.pennbenedict.data.model


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Aqi(
    @Json(name = "status") val status: String?,
    @Json(name = "data") val dataValue: Data?
) {
    @JsonClass(generateAdapter = true)
    data class Data(
        @Json(name = "aqi") val airQualityIndex: String,
        @Json(name = "city") val city: City,
        @Json(name = "attributions") val attributions: List<Attributions>,
        @Json(name = "forecast") val forecast: Forecast,
    ) {

        @JsonClass(generateAdapter = true)
        data class Attributions(
            @Json(name = "name") val stationName: String?//Weather station name
        )

        @JsonClass(generateAdapter = true)
        data class City(
            @Json(name = "geo") val geo: List<Double>,
            @Json(name = "name") val cityName: String?
        )

        @JsonClass(generateAdapter = true)
        data class Forecast(
            @Json(name = "daily") val daily: Daily?
        )
        @JsonClass(generateAdapter = true)
        data class Daily(
            @Json(name = "o3") val o3: List<O3>
        )
        @JsonClass(generateAdapter = true)
        data class O3(
            @Json(name = "avg") val avg: String?
        )
    }
}