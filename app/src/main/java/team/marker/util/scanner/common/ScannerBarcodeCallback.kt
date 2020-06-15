package team.marker.util.scanner.common

import com.google.zxing.ResultPoint

interface ScannerBarcodeCallback {

    fun barcodeResult(result: ScannerBarcodeResultMultiple)
    fun possibleResultPoints(resultPoints: List<ResultPoint>)
}