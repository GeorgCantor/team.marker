package team.marker.util.scanner.reader

import team.marker.util.scanner.common.ScannerResultPoint

/**
 * Meta-data container for QR Code decoding. Instances of this class may be used to convey information back to the
 * decoding caller. Callers are expected to process this.
 *
 * @see com.google.zxing.common.DecoderResult.getOther
 */
class ScannerQRCodeDecoderMetaData internal constructor(
    /**
     * @return true if the QR Code was mirrored.
     */
    val isMirrored: Boolean
) {

    /**
     * Apply the result points' order correction due to mirroring.
     *
     * @param points Array of points to apply mirror correction to.
     */
    fun applyMirroredCorrection(points: Array<ScannerResultPoint?>?) {
        if (!isMirrored || points == null || points.size < 3) {
            return
        }
        val bottomLeft = points[0]
        points[0] = points[2]
        points[2] = bottomLeft
        // No need to 'fix' top-left and alignment pattern.
    }
}
