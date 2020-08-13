package team.marker.model.requests

import com.google.gson.annotations.SerializedName

data class PickRequest (
    @SerializedName("product_ids") val product_ids: MutableList<String>?,
    @SerializedName("email") val email: String?
)