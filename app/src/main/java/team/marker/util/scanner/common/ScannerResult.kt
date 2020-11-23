package team.marker.util.scanner.common

import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultMetadataType
import com.google.zxing.ResultPoint
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
    var resultPoints: Array<ResultPoint?>?,
    /**
     * @return [BarcodeFormat] representing the format of the barcode that was decoded
     */
    val barcodeFormat: BarcodeFormat?,
    val timestamp: Long
) {

    var resultMetadata: MutableMap<ResultMetadataType, Any>? =
        null

    @JvmOverloads
    constructor(
        text: String,
        rawBytes: ByteArray?,
        resultPoints: Array<ResultPoint?>?,
        format: BarcodeFormat?,
        timestamp: Long = System.currentTimeMillis()
    ) : this(
        text, rawBytes, if (rawBytes == null) 0 else 8 * rawBytes.size,
        resultPoints, format, timestamp
    ) {
    }

    /**
     * @return [Map] mapping [ResultMetadataType] keys to values. May be
     * `null`. This contains optional metadata about what was detected about the barcode,
     * like orientation.
     */
    @JvmName("getResultMetadata1")
    fun getResultMetadata(): Map<ResultMetadataType, Any>? {
        return resultMetadata
    }

    fun putMetadata(type: ResultMetadataType, value: Any) {
        if (resultMetadata == null) {
            resultMetadata = EnumMap(ResultMetadataType::class.java)
        }
        resultMetadata!![type] = value
    }

    fun putAllMetadata(metadata: MutableMap<ResultMetadataType, Any>?) {
        if (metadata != null) {
            if (resultMetadata == null) {
                resultMetadata = metadata
            } else {
                resultMetadata!!.putAll(metadata)
            }
        }
    }

    fun addResultPoints(newPoints: Array<ResultPoint?>?) {
        val oldPoints = resultPoints
        if (oldPoints == null) {
            resultPoints = newPoints
        } else if (newPoints != null && newPoints.size > 0) {
            val allPoints = arrayOfNulls<ResultPoint>(oldPoints.size + newPoints.size)
            System.arraycopy(oldPoints, 0, allPoints, 0, oldPoints.size)
            System.arraycopy(newPoints, 0, allPoints, oldPoints.size, newPoints.size)
            resultPoints = allPoints
        }
    }

    override fun toString(): String {
        return text
    }
}
