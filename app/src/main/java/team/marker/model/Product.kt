package team.marker.model

import android.graphics.Rect

data class Product(
    val id: Int,
    val name: String,
    var rectName: Rect? = null,
    var rectButton: Rect? = null,
    var clickStatus: Int = 0,
    var isVisible: Boolean = false,
)