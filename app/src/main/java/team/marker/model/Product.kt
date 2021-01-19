package team.marker.model

import android.graphics.Rect

data class Product(
    val id: Int,
    val name: String,
    var rect: Rect? = null,
    var clickStatus: Int = 0
)