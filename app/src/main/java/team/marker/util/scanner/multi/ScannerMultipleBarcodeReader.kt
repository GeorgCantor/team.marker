package team.marker.util.scanner.multi

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.Result
import team.marker.util.scanner.common.ScannerNotFoundException

interface ScannerMultipleBarcodeReader {
    @Throws(ScannerNotFoundException::class)
    fun decodeMultiple(image: BinaryBitmap): Array<Result?>?

    @Throws(ScannerNotFoundException::class)
    fun decodeMultiple(image: BinaryBitmap, hints: MutableMap<DecodeHintType, Any?>?): Array<Result?>?
}