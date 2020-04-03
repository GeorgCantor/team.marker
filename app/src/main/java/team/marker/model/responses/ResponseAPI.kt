package team.marker.model.responses

import com.google.gson.annotations.SerializedName

data class ResponseAPI<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("response") val response: T?,
    @SerializedName("error") val error: ResponseError?
)