package team.marker.model.requests

import com.google.gson.annotations.SerializedName

data class BreachRequest (
    @SerializedName("product_id") val product_id: Int?,
    @SerializedName("reason_id") val reason_id: Int?,
    @SerializedName("user_reason") val user_reason: String?,
    @SerializedName("comment") val comment: String?
)