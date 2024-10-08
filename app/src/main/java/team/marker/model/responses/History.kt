package team.marker.model.responses

import com.google.gson.annotations.SerializedName

data class History(
    @SerializedName("info") val info: MutableList<HistoryItem>? = null,
    @SerializedName("next_offset") val next_id: Int?
)