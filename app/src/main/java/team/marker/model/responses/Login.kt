package team.marker.model.responses

import com.google.gson.annotations.SerializedName

data class Login(
    @SerializedName("sid") val sid: Int? = null,
    @SerializedName("token") val token: String? = null
)