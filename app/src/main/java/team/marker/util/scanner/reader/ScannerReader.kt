package team.marker.util.scanner.reader

import com.google.zxing.*
import team.marker.util.scanner.common.*

interface ScannerReader {

    @Throws(ScannerNotFoundException::class, ScannerChecksumException::class, ScannerFormatException::class)
    fun decode(image: BinaryBitmap): Result

    @Throws(ScannerNotFoundException::class, ScannerChecksumException::class, ScannerFormatException::class)
    fun decode(image: BinaryBitmap, hints: MutableMap<DecodeHintType, Any?>?): Result

    @Throws(ScannerNotFoundException::class, ScannerChecksumException::class, ScannerFormatException::class)
    fun decodeMultiple(image: BinaryBitmap, hints: MutableMap<DecodeHintType, Any?>?): Array<Result?>

    fun reset()
}