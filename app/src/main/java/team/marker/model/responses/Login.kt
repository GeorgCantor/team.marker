package team.marker.model.responses

import com.google.gson.annotations.SerializedName
import team.marker.util.Constants.SID
import team.marker.util.Constants.TOKEN

data class Login(
    @SerializedName(SID) val sid: String? = null,
    @SerializedName(TOKEN) val token: String? = null
)