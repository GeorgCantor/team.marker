package team.marker.util.scanner.common

import com.google.zxing.common.BitArray
import com.google.zxing.common.BitMatrix

/**
 * This class hierarchy provides a set of methods to convert luminance data to 1 bit data.
 * It allows the algorithm to vary polymorphically, for example allowing a very expensive
 * thresholding technique for servers and a fast one for mobile. It also permits the implementation
 * to vary, e.g. a JNI version for Android and a Java fallback version for other platforms.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
abstract class ScannerBinarizer protected constructor(val luminanceSource: ScannerLuminanceSource) {

    @Throws(ScannerNotFoundException::class)
    abstract fun getBlackRow(y: Int, row: BitArray?): BitArray?

    @get:Throws(ScannerNotFoundException::class)
    abstract val blackMatrix: BitMatrix?

    /**
     * Creates a new object with the same type as this Binarizer implementation, but with pristine
     * state. This is needed because Binarizer implementations may be stateful, e.g. keeping a cache
     * of 1 bit data. See Effective Java for why we can't use Java's clone() method.
     *
     * @param source The LuminanceSource this Binarizer will operate on.
     * @return A new concrete Binarizer implementation object.
     */
    abstract fun createBinarizer(source: ScannerLuminanceSource?): ScannerBinarizer?
    val width: Int
        get() = luminanceSource.width
    val height: Int
        get() = luminanceSource.height

}