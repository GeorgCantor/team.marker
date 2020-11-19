package team.marker.model.requests

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PickProduct(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("quantity") var quantity: Double = 0.0,
    @SerializedName("type") var type: Int? = null
) : Parcelable