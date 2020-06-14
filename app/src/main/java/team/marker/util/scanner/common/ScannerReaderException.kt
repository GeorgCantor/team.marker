package team.marker.util.scanner.common

abstract class ScannerReaderException : Exception {

    internal constructor() { }
    internal constructor(cause: Throwable?) : super(cause) {}

    @Synchronized
    override fun fillInStackTrace(): Throwable? {
        return null
    }

    companion object {
        val isStackTrace = System.getProperty("surefire.test.class.path") != null
        val NO_TRACE = arrayOfNulls<StackTraceElement>(0)
    }
}