package team.marker.util.scanner.common

class ScannerNotFoundException : ScannerReaderException() {
    val INSTANCE = ScannerNotFoundException()

    fun getNotFoundInstance(): ScannerNotFoundException? {
        return INSTANCE
    }

    init {
        INSTANCE.stackTrace = NO_TRACE
    }
}