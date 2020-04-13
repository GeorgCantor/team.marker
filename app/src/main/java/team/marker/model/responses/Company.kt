package team.marker.model.responses

import com.google.gson.annotations.SerializedName

data class Company(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("address_lat") val address_lat: Double? = null,
    @SerializedName("address_lng") val address_lng: Double? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null
)