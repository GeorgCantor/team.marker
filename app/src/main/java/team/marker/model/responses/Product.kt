package team.marker.model.responses

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    // simple
    @SerializedName("id") val id: Int? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("destination") val destination: String? = null,
    @SerializedName("produced") val produced: String? = null,
    @SerializedName("shipped") val shipped: String? = null,
    @SerializedName("partner_product_id") val partnerProductId: String? = null,
    @SerializedName("partner_title") val partnerTitle: String? = null,
    // arrays
    @SerializedName("manufacturer") val manufacturer: Company? = null,
    @SerializedName("customer") val customer: Company? = null,
    @SerializedName("consignee") val consignee: Company? = null,
    @SerializedName("options") val options: MutableList<ProductOption>? = null,
    @SerializedName("files") val files: MutableList<ProductFile>? = null,
    @SerializedName("contract") val contract: Contract? = null
) : Parcelable