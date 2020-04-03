package team.marker.model.requests

import com.google.gson.annotations.SerializedName

data class ProductRequest (
    @SerializedName("product_id") val product_id: String?
)