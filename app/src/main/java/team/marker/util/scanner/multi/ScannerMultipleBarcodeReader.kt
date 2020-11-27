package team.marker.util.scanner.multi

import com.google.zxing.BinaryBitmap
import team.marker.util.scanner.common.ScannerNotFoundException
import team.marker.util.scanner.common.ScannerResult
import team.marker.util.scanner.decoder.ScannerDecodeHintType

interface ScannerMultipleBarcodeReader {
    @Throws(ScannerNotFoundException::class)
    fun decodeMultiple(image: BinaryBitmap): Array<ScannerResult?>?

    @Throws(ScannerNotFoundException::class)
    fun decodeMultiple(image: BinaryBitmap, hints: MutableMap<ScannerDecodeHintType?, Any?>?): Array<ScannerResult?>?
}