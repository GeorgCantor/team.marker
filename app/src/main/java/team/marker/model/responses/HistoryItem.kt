package team.marker.model.responses

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class HistoryItem(
    val id: Int,
    val created: String
) : Parcelable