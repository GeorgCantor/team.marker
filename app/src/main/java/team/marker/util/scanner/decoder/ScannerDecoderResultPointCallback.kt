package team.marker.util.scanner.decoder

import com.google.zxing.ResultPoint
import com.google.zxing.ResultPointCallback
import team.marker.util.scanner.common.ScannerResultPoint
import team.marker.util.scanner.common.ScannerResultPointCallback
import team.marker.util.scanner.decoder.ScannerDecoder

class ScannerDecoderResultPointCallback : ResultPointCallback {
    var decoder: ScannerDecoder? = null

    constructor(decoder: ScannerDecoder?) {
        this.decoder = decoder
    }

    constructor() {}

    override fun foundPossibleResultPoint(point: ResultPoint) {
        if (decoder != null) {
            decoder!!.foundPossibleResultPoint(point)
        }
    }
}