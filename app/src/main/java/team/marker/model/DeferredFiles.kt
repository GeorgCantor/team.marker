package team.marker.model

data class DeferredFiles(
    val productId: Int,
    val reasonId: Int,
    val userReason: String,
    val comment: String,
    val filePaths: List<String>
)