package team.marker.model.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Contract(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("annex_number") val annex_number: String? = null,
    @SerializedName("annex_date") val annex_date: String? = null
) : Parcelable