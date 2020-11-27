package team.marker.util.scanner.decoder

import com.google.zxing.ResultPointCallback

enum class ScannerDecodeHintType(
    /**
     * Data type the hint is expecting.
     * Among the possible values the [Void] stands out as being used for
     * hints that do not expect a value to be supplied (flag hints). Such hints
     * will possibly have their value ignored, or replaced by a
     * [Boolean.TRUE]. Hint suppliers should probably use
     * [Boolean.TRUE] as directed by the actual hint documentation.
     */
    val valueType: Class<*>
) {
    /**
     * Unspecified, application-specific hint. Maps to an unspecified [Object].
     */
    OTHER(Any::class.java),

    /**
     * Image is a pure monochrome image of a barcode. Doesn't matter what it maps to;
     * use [Boolean.TRUE].
     */
    PURE_BARCODE(Void::class.java),

    /**
     * Image is known to be of one of a few possible formats.
     * Maps to a [List] of [BarcodeFormat]s.
     */
    POSSIBLE_FORMATS(MutableList::class.java),

    /**
     * Spend more time to try to find a barcode; optimize for accuracy, not speed.
     * Doesn't matter what it maps to; use [Boolean.TRUE].
     */
    TRY_HARDER(Void::class.java),

    /**
     * Specifies what character encoding to use when decoding, where applicable (type String)
     */
    CHARACTER_SET(String::class.java),

    /**
     * Allowed lengths of encoded data -- reject anything else. Maps to an `int[]`.
     */
    ALLOWED_LENGTHS(IntArray::class.java),

    /**
     * Assume Code 39 codes employ a check digit. Doesn't matter what it maps to;
     * use [Boolean.TRUE].
     */
    ASSUME_CODE_39_CHECK_DIGIT(Void::class.java),

    /**
     * Assume the barcode is being processed as a GS1 barcode, and modify behavior as needed.
     * For example this affects FNC1 handling for Code 128 (aka GS1-128). Doesn't matter what it maps to;
     * use [Boolean.TRUE].
     */
    ASSUME_GS1(Void::class.java),

    /**
     * If true, return the start and end digits in a Codabar barcode instead of stripping them. They
     * are alpha, whereas the rest are numeric. By default, they are stripped, but this causes them
     * to not be. Doesn't matter what it maps to; use [Boolean.TRUE].
     */
    RETURN_CODABAR_START_END(Void::class.java),

    /**
     * The caller needs to be notified via callback when a possible [ResultPoint]
     * is found. Maps to a [ResultPointCallback].
     */
    NEED_RESULT_POINT_CALLBACK(ResultPointCallback::class.java),

    /**
     * Allowed extension lengths for EAN or UPC barcodes. Other formats will ignore this.
     * Maps to an `int[]` of the allowed extension lengths, for example [2], [5], or [2, 5].
     * If it is optional to have an extension, do not set this hint. If this is set,
     * and a UPC or EAN barcode is found but an extension is not, then no result will be returned
     * at all.
     */
    ALLOWED_EAN_EXTENSIONS(IntArray::class.java);
    // End of enumeration values.

}
