package team.marker.util.scanner.common

class ScannerFormatException : ScannerReaderException {

    private constructor()
    private constructor(cause: Throwable?) : super(cause)

    companion object {
        private val INSTANCE = ScannerFormatException()
        val formatInstance: ScannerFormatException
            get() = if (isStackTrace) ScannerFormatException() else INSTANCE

        fun getFormatInstance(cause: Throwable?): ScannerFormatException {
            return if (isStackTrace) ScannerFormatException(cause) else INSTANCE
        }

        init {
            INSTANCE.stackTrace = NO_TRACE
        }
    }
}