package team.marker.model.requests

import com.google.gson.annotations.SerializedName

class CargoRequest(
    @SerializedName("places") val places: Array<IntArray>,
    @SerializedName("consignor_id") val consignor_id: Int,
    @SerializedName("consignee_id") val consignee_id: Int,
    @SerializedName("carrier_id") val carrier_id: Int,
    @SerializedName("customer_id") val customer_id: Int,
    @SerializedName("cost_delivery") val cost_delivery: String,
    @SerializedName("cost_total") val cost_total: String,
    @SerializedName("weight") val weight: String,
    @SerializedName("driver_first_name") val driver_first_name: String,
    @SerializedName("driver_last_name") val driver_last_name: String,
    @SerializedName("driver_middle_name") val driver_middle_name: String,
    @SerializedName("driver_phone") val driver_phone: String,
    @SerializedName("vehicle_type") val vehicle_type: Int,
    @SerializedName("vehicle_brand") val vehicle_brand: String,
    @SerializedName("vehicle_number") val vehicle_number: String
)