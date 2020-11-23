package team.marker.util.scanner.decoder

import team.marker.util.scanner.common.ScannerResultPoint

class ScannerQRCodeDecoderMetaData internal constructor(private val isMirrored: Boolean) {

    fun applyMirroredCorrection(points: Array<ScannerResultPoint?>?) {
        if (!isMirrored || points == null || points.size < 3) return
        val bottomLeft = points[0]
        points[0] = points[2]
        points[2] = bottomLeft
    }

}