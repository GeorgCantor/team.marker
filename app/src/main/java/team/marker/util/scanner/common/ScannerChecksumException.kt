package team.marker.util.scanner.common

class ScannerChecksumException : ScannerReaderException {

    private constructor()
    private constructor(cause: Throwable?) : super(cause)

    companion object {
        private val INSTANCE = ScannerChecksumException()

        val checksumInstance: ScannerChecksumException
            get() = if (isStackTrace) ScannerChecksumException() else INSTANCE

        fun getChecksumInstance(cause: Throwable?): ScannerChecksumException {
            return if (isStackTrace) ScannerChecksumException(cause) else INSTANCE
        }

        init {
            INSTANCE.stackTrace = NO_TRACE
        }
    }
}