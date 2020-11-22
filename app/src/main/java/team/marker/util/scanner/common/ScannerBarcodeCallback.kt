package team.marker.util.scanner.common

interface ScannerBarcodeCallback {

    fun barcodeResult(result: ScannerBarcodeResultMultiple)
    fun possibleResultPoints(scannerResultPoints: List<ScannerResultPoint>)
}