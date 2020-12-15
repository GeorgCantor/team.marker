package team.marker.model.requests

import com.google.gson.annotations.SerializedName
import team.marker.util.Constants.PRODUCTS

data class PickRequest (
    @SerializedName(PRODUCTS) val products: MutableList<PickProduct>?,
    @SerializedName("email") val email: String?
)