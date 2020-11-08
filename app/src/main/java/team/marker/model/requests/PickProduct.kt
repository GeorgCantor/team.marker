package team.marker.model.requests

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PickProduct(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("quantity") var quantity: String? = null,
    @SerializedName("type") var type: Int? = null
) : Parcelable