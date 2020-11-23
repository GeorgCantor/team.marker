package team.marker.util.scanner.decoder

import team.marker.util.scanner.common.ScannerResultPoint
import team.marker.util.scanner.common.ScannerResultPointCallback

class ScannerDecoderResultPointCallback : ScannerResultPointCallback {
    var decoder: ScannerDecoder? = null

    constructor(decoder: ScannerDecoder?) {
        this.decoder = decoder
    }

    constructor() {}

    override fun foundPossibleResultPoint(point: ScannerResultPoint?) {
        if (decoder != null) {
            decoder!!.foundPossibleResultPoint(point)
        }
    }
}