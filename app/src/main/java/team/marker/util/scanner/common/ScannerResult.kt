package team.marker.util.scanner.common

import java.util.*

/**
 *
 * Encapsulates the result of decoding a barcode within an image.
 *
 * @author Sean Owen
 */
class ScannerResult(
    /**
     * @return raw text encoded by the barcode
     */
    val text: String,
    /**
     * @return raw bytes encoded by the barcode, if applicable, otherwise `null`
     */
    val rawBytes: ByteArray?,
    /**
     * @return how many bits of [.getRawBytes] are valid; typically 8 times its length
     * @since 3.3.0
     */
    val numBits: Int,
    /**
     * @return points related to the barcode in the image. These are typically points
     * identifying finder patterns or the corners of the barcode. The exact meaning is
     * specific to the type of barcode that was decoded.
     */
    var resultPoints: Array<ScannerResultPoint?>?,
    /**
     * @return [BarcodeFormat] representing the format of the barcode that was decoded
     */
    val barcodeFormat: ScannerBarcodeFormat?,
    val timestamp: Long
) {

    var resultMetadata: MutableMap<ScannerResultMetadataType, Any>? =
        null

    @JvmOverloads
    constructor(
        text: String,
        rawBytes: ByteArray?,
        resultPoints: Array<ScannerResultPoint?>?,
        format: ScannerBarcodeFormat?,
        timestamp: Long = System.currentTimeMillis()
    ) : this(
        text, rawBytes, if (rawBytes == null) 0 else 8 * rawBytes.size,
        resultPoints, format, timestamp
    ) {
    }

    @JvmName("getResultMetadata1")
    fun getResultMetadata(): Map<ScannerResultMetadataType, Any>? {
        return resultMetadata
    }

    fun putMetadata(type: ScannerResultMetadataType, value: Any) {
        if (resultMetadata == null) {
            resultMetadata = EnumMap(ScannerResultMetadataType::class.java)
        }
        resultMetadata!![type] = value
    }

    fun putAllMetadata(metadata: MutableMap<ScannerResultMetadataType, Any>?) {
        if (metadata != null) {
            if (resultMetadata == null) {
                resultMetadata = metadata
            } else {
                resultMetadata!!.putAll(metadata)
            }
        }
    }

    fun addResultPoints(newPoints: Array<ScannerResultPoint?>?) {
        val oldPoints = resultPoints
        if (oldPoints == null) {
            resultPoints = newPoints
        } else if (newPoints != null && newPoints.size > 0) {
            val allPoints = arrayOfNulls<ScannerResultPoint>(oldPoints.size + newPoints.size)
            System.arraycopy(oldPoints, 0, allPoints, 0, oldPoints.size)
            System.arraycopy(newPoints, 0, allPoints, oldPoints.size, newPoints.size)
            resultPoints = allPoints
        }
    }

    override fun toString(): String {
        return text
    }
}
