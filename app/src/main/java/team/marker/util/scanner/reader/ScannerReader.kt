package team.marker.util.scanner.reader

import com.google.zxing.BinaryBitmap
import team.marker.util.scanner.common.ScannerChecksumException
import team.marker.util.scanner.common.ScannerFormatException
import team.marker.util.scanner.common.ScannerNotFoundException
import team.marker.util.scanner.common.ScannerResult
import team.marker.util.scanner.decoder.ScannerDecodeHintType

interface ScannerReader {

    @Throws(ScannerNotFoundException::class, ScannerChecksumException::class, ScannerFormatException::class)
    fun decode(image: BinaryBitmap): ScannerResult

    @Throws(ScannerNotFoundException::class, ScannerChecksumException::class, ScannerFormatException::class)
    fun decodeMultiple(image: BinaryBitmap): Array<ScannerResult?>

    @Throws(ScannerNotFoundException::class, ScannerChecksumException::class, ScannerFormatException::class)
    fun decode(image: BinaryBitmap, hints: MutableMap<ScannerDecodeHintType?, Any?>?): ScannerResult

    @Throws(ScannerNotFoundException::class, ScannerChecksumException::class, ScannerFormatException::class)
    fun decodeMultiple(image: BinaryBitmap, hints: MutableMap<ScannerDecodeHintType?, Any?>?): Array<ScannerResult?>

    fun reset()
}