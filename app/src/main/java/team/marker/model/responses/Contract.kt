package team.marker.model.responses

import com.google.gson.annotations.SerializedName

data class Contract(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("annex_number") val annex_number: String? = null,
    @SerializedName("annex_date") val annex_date: String? = null
)