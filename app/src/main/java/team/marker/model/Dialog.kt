package team.marker.model

data class Dialog(
    val title: String,
    val message: String? = null,
    val posText: String? = null,
    val negText: String? = null,
    val action: () -> (Unit)
)