package team.marker.util.scanner.decoder

import com.google.zxing.ResultPoint

class ScannerQRCodeDecoderMetaData internal constructor(private val isMirrored: Boolean) {

    fun applyMirroredCorrection(points: Array<ResultPoint>?) {
        if (!isMirrored || points == null || points.size < 3) return
        val bottomLeft = points[0]
        points[0] = points[2]
        points[2] = bottomLeft
    }

}