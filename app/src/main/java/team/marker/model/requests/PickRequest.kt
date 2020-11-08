package team.marker.model.requests

import com.google.gson.annotations.SerializedName

data class PickRequest (
    @SerializedName("products") val products: MutableList<PickProduct>?,
    @SerializedName("email") val email: String?
)