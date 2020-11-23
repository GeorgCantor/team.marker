package team.marker.util.scanner.multi

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import team.marker.util.scanner.common.ScannerNotFoundException
import team.marker.util.scanner.common.ScannerResult

interface ScannerMultipleBarcodeReader {
    @Throws(ScannerNotFoundException::class)
    fun decodeMultiple(image: BinaryBitmap): Array<ScannerResult?>?

    @Throws(ScannerNotFoundException::class)
    fun decodeMultiple(image: BinaryBitmap, hints: MutableMap<DecodeHintType, Any?>?): Array<ScannerResult?>?
}