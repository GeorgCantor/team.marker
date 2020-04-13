package team.marker.model.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProductFile(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("type") val type: Int? = null,
    @SerializedName("path") val path: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("size") val size: String? = null
) : Parcelable