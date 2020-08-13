package team.marker.model.responses

import com.google.gson.annotations.SerializedName

data class Products(
    @SerializedName("info") val info: MutableList<Product>? = null
)