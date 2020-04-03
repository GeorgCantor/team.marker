package team.marker.model.responses

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_id") val user_id: Int? = null,
    @SerializedName("first_name") val first_name: String? = null,
    @SerializedName("last_name") val last_name: String? = null
)