package team.marker.model.responses

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("company_title") val company_title: String? = null,
    @SerializedName("company_address") val company_address: String? = null,
    @SerializedName("produced") val produced: String? = null
)