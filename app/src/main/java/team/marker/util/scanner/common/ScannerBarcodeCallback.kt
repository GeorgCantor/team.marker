package team.marker.util.scanner.common

import team.marker.util.scanner.ResultPoint

interface ScannerBarcodeCallback {

    fun barcodeResult(result: ScannerBarcodeResultMultiple)
    fun possibleResultPoints(resultPoints: List<ResultPoint>)
}