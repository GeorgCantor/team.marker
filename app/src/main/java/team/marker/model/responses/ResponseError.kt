package team.marker.model.responses

import com.google.gson.annotations.SerializedName

class ResponseError {
    @SerializedName("error_code") val error_code: Int? = null
    @SerializedName("error_msg") val error_msg: String? = null
    @SerializedName("error_data") val error_data: HashMap<String, String>? = null
}