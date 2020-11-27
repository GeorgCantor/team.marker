package team.marker.util.scanner.decoder


/**
 *
 * Encapsulates the result of decoding a matrix of bits. This typically
 * applies to 2D barcode formats. For now it contains the raw bytes obtained,
 * as well as a String interpretation of those bytes, if applicable.
 *
 * @author Sean Owen
 */
class DecoderResult @JvmOverloads constructor(
    /**
     * @return raw bytes representing the result, or `null` if not applicable
     */
    val rawBytes: ByteArray?,
    text: String,
    byteSegments: List<ByteArray>,
    ecLevel: String,
    saSequence: Int = -1,
    saParity: Int = -1
) {
    /**
     * @return how many bits of [.getRawBytes] are valid; typically 8 times its length
     * @since 3.3.0
     */
    /**
     * @param numBits overrides the number of bits that are valid in [.getRawBytes]
     * @since 3.3.0
     */
    var numBits: Int

    /**
     * @return text representation of the result
     */
    val text: String

    /**
     * @return list of byte segments in the result, or `null` if not applicable
     */
    val byteSegments: List<ByteArray>

    /**
     * @return name of error correction level used, or `null` if not applicable
     */
    val eCLevel: String

    /**
     * @return number of errors corrected, or `null` if not applicable
     */
    var errorsCorrected: Int? = null

    /**
     * @return number of erasures corrected, or `null` if not applicable
     */
    var erasures: Int? = null

    /**
     * @return arbitrary additional metadata
     */
    var other: Any? = null
    val structuredAppendParity: Int
    val structuredAppendSequenceNumber: Int

    fun hasStructuredAppend(): Boolean {
        return structuredAppendParity >= 0 && structuredAppendSequenceNumber >= 0
    }

    init {
        numBits = if (rawBytes == null) 0 else 8 * rawBytes.size
        this.text = text
        this.byteSegments = byteSegments
        eCLevel = ecLevel
        structuredAppendParity = saParity
        structuredAppendSequenceNumber = saSequence
    }
}
