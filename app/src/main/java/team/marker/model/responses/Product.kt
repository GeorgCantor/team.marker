package team.marker.model.responses

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("produced") val produced: String? = null,
    @SerializedName("shipped") val shipped: String? = null,

    @SerializedName("manufacturer") val manufacturer: Company? = null,
    @SerializedName("customer") val customer: Company? = null,
    @SerializedName("options") val options: MutableList<ProductOption>? = null,
    @SerializedName("files") val files: MutableList<ProductFile>? = null,
    @SerializedName("contract") val contract: Contract? = null
)