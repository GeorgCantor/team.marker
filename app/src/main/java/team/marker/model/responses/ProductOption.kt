package team.marker.model.responses

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProductOption(
    @SerializedName("id")
    @Expose
    val id: Int? = null,
    @SerializedName("title")
    @Expose
    val title: String? = null,
    @SerializedName("value")
    @Expose
    val value: String? = null,
    @SerializedName("units_title")
    @Expose
    val units_title: String? = null
) : Parcelable