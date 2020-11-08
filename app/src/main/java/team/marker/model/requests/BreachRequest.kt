package team.marker.model.requests

import com.google.gson.annotations.SerializedName

data class BreachRequest (
    @SerializedName("product_id") val product_id: String?,
    @SerializedName("description") val description: String?
)