package team.marker.util.scanner.reader

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.NotFoundException
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.DecoderResult
import com.google.zxing.qrcode.decoder.Decoder
import com.google.zxing.qrcode.decoder.QRCodeDecoderMetaData
import team.marker.util.scanner.common.*
import team.marker.util.scanner.detector.ScannerDetector
import kotlin.math.roundToInt

abstract class ScannerQRCodeReader : ScannerReader {
    protected val decoder = Decoder()

    @Throws(ScannerNotFoundException::class, ScannerChecksumException::class, ScannerFormatException::class)
    override fun decode(image: BinaryBitmap): ScannerResult {
        return decode(image, null)
    }

    @Throws(ScannerNotFoundException::class, ScannerChecksumException::class, ScannerFormatException::class)
    override fun decode(image: BinaryBitmap, hints: MutableMap<DecodeHintType, Any?>?): ScannerResult {
        val decoderResult: DecoderResult
        val points: Array<ScannerResultPoint?>
        if (hints != null && hints.containsKey(DecodeHintType.PURE_BARCODE)) {
            val bits = extractPureBits(image.blackMatrix)
            decoderResult = decoder.decode(bits, hints)
            points = NO_POINTS
        } else {
            val detectorResult = ScannerDetector(image.blackMatrix).detect(hints)
            decoderResult = decoder.decode(detectorResult.bits, hints)
            points = detectorResult.points!!
        }

        // If the code was mirrored: swap the bottom-left and the top-right points.
        if (decoderResult.other is QRCodeDecoderMetaData) (decoderResult.other as ScannerQRCodeDecoderMetaData).applyMirroredCorrection(points)
        val result = ScannerResult(decoderResult.text, decoderResult.rawBytes, points, ScannerBarcodeFormat.QR_CODE)
        val byteSegments = decoderResult.byteSegments
        if (byteSegments != null) result.putMetadata(ScannerResultMetadataType.BYTE_SEGMENTS, byteSegments)
        val ecLevel = decoderResult.ecLevel
        if (ecLevel != null) result.putMetadata(ScannerResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel)
        if (decoderResult.hasStructuredAppend()) {
            result.putMetadata(ScannerResultMetadataType.STRUCTURED_APPEND_SEQUENCE, decoderResult.structuredAppendSequenceNumber)
            result.putMetadata(ScannerResultMetadataType.STRUCTURED_APPEND_PARITY, decoderResult.structuredAppendParity)
        }
        return result
    }

    override fun reset() {
        // do nothing
    }

    companion object {
        private val NO_POINTS = arrayOfNulls<ScannerResultPoint>(0)

        @Throws(NotFoundException::class)
        private fun extractPureBits(image: BitMatrix): BitMatrix {
            val leftTopBlack = image.topLeftOnBit
            val rightBottomBlack = image.bottomRightOnBit
            if (leftTopBlack == null || rightBottomBlack == null) throw NotFoundException.getNotFoundInstance()
            val moduleSize: Float = moduleSize(leftTopBlack, image)
            var top = leftTopBlack[1]
            val bottom = rightBottomBlack[1]
            var left = leftTopBlack[0]
            var right = rightBottomBlack[0]

            // Sanity check!
            if (left >= right || top >= bottom) throw NotFoundException.getNotFoundInstance()
            if (bottom - top != right - left) {
                right = left + (bottom - top)
                if (right >= image.width) throw NotFoundException.getNotFoundInstance()
            }
            val matrixWidth = ((right - left + 1) / moduleSize).roundToInt()
            val matrixHeight = ((bottom - top + 1) / moduleSize).roundToInt()
            if (matrixWidth <= 0 || matrixHeight <= 0) throw NotFoundException.getNotFoundInstance()
            if (matrixHeight != matrixWidth) throw NotFoundException.getNotFoundInstance()

            val nudge = (moduleSize / 2.0f).toInt()
            top += nudge
            left += nudge

            val nudgedTooFarRight = left + ((matrixWidth - 1) * moduleSize).toInt() - right
            if (nudgedTooFarRight > 0) {
                if (nudgedTooFarRight > nudge) throw NotFoundException.getNotFoundInstance()
                left -= nudgedTooFarRight
            }
            // See logic above
            val nudgedTooFarDown = top + ((matrixHeight - 1) * moduleSize).toInt() - bottom
            if (nudgedTooFarDown > 0) {
                if (nudgedTooFarDown > nudge) throw NotFoundException.getNotFoundInstance()
                top -= nudgedTooFarDown
            }

            val bits = BitMatrix(matrixWidth, matrixHeight)
            for (y in 0 until matrixHeight) {
                val iOffset = top + (y * moduleSize).toInt()
                for (x in 0 until matrixWidth) {
                    if (image[left + (x * moduleSize).toInt(), iOffset]) bits[x] = y
                }
            }
            return bits
        }

        @Throws(NotFoundException::class)
        private fun moduleSize(leftTopBlack: IntArray, image: BitMatrix): Float {
            val height = image.height
            val width = image.width
            var x = leftTopBlack[0]
            var y = leftTopBlack[1]
            var inBlack = true
            var transitions = 0
            while (x < width && y < height) {
                if (inBlack != image[x, y]) {
                    if (++transitions == 5) break
                    inBlack = !inBlack
                }
                x++
                y++
            }
            if (x == width || y == height) throw NotFoundException.getNotFoundInstance()
            return (x - leftTopBlack[0]) / 7.0f
        }
    }
}