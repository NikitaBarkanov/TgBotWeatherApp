package data.remote.models

data class ReversedCountry(
    val address: Address,
    val boundingbox: List<String>,
    val display_name: String,
    val lat: String,
    val licence: String,
    val lon: String,
    val osm_id: Long,
    val osm_type: String,
    val place_id: Int
)