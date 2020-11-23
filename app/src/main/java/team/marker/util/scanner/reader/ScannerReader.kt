package team.marker.util.scanner.reader

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import team.marker.util.scanner.common.ScannerChecksumException
import team.marker.util.scanner.common.ScannerFormatException
import team.marker.util.scanner.common.ScannerNotFoundException
import team.marker.util.scanner.common.ScannerResult

interface ScannerReader {

    @Throws(ScannerNotFoundException::class, ScannerChecksumException::class, ScannerFormatException::class)
    fun decode(image: BinaryBitmap): ScannerResult

    @Throws(ScannerNotFoundException::class, ScannerChecksumException::class, ScannerFormatException::class)
    fun decodeMultiple(image: BinaryBitmap): Array<ScannerResult?>

    @Throws(ScannerNotFoundException::class, ScannerChecksumException::class, ScannerFormatException::class)
    fun decode(image: BinaryBitmap, hints: MutableMap<DecodeHintType, Any?>?): ScannerResult

    @Throws(ScannerNotFoundException::class, ScannerChecksumException::class, ScannerFormatException::class)
    fun decodeMultiple(image: BinaryBitmap, hints: MutableMap<DecodeHintType, Any?>?): Array<ScannerResult?>

    fun reset()
}