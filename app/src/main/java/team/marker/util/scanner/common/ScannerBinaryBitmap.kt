package team.marker.util.scanner.common

import com.google.zxing.common.BitArray
import com.google.zxing.common.BitMatrix

/**
 * This class is the core bitmap class used by ZXing to represent 1 bit data. Reader objects
 * accept a BinaryBitmap and attempt to decode it.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
class ScannerBinaryBitmap(binarizer: ScannerBinarizer?) {
    private val binarizer: ScannerBinarizer
    private var matrix: BitMatrix? = null

    /**
     * @return The width of the bitmap.
     */
    val width: Int
        get() = binarizer.width

    /**
     * @return The height of the bitmap.
     */
    val height: Int
        get() = binarizer.height

    @Throws(ScannerNotFoundException::class)
    fun getBlackRow(y: Int, row: BitArray?): BitArray {
        return binarizer.getBlackRow(y, row)!!
    }

    @get:Throws(ScannerNotFoundException::class)
    val blackMatrix: BitMatrix?
        get() {
            // The matrix is created on demand the first time it is requested, then cached. There are two
            // reasons for this:
            // 1. This work will never be done if the caller only installs 1D Reader objects, or if a
            //    1D Reader finds a barcode before the 2D Readers run.
            // 2. This work will only be done once even if the caller installs multiple 2D Readers.
            if (matrix == null) {
                matrix = binarizer.blackMatrix
            }
            return matrix
        }

    /**
     * @return Whether this bitmap can be cropped.
     */
    val isCropSupported: Boolean
        get() = binarizer.luminanceSource.isCropSupported

    /**
     * Returns a new object with cropped image data. Implementations may keep a reference to the
     * original data rather than a copy. Only callable if isCropSupported() is true.
     *
     * @param left The left coordinate, which must be in [0,getWidth())
     * @param top The top coordinate, which must be in [0,getHeight())
     * @param width The width of the rectangle to crop.
     * @param height The height of the rectangle to crop.
     * @return A cropped version of this object.
     */
    fun crop(left: Int, top: Int, width: Int, height: Int): ScannerBinaryBitmap {
        val newSource = binarizer.luminanceSource.crop(left, top, width, height)
        return ScannerBinaryBitmap(binarizer.createBinarizer(newSource))
    }

    /**
     * @return Whether this bitmap supports counter-clockwise rotation.
     */
    val isRotateSupported: Boolean
        get() = binarizer.luminanceSource.isRotateSupported

    /**
     * Returns a new object with rotated image data by 90 degrees counterclockwise.
     * Only callable if [.isRotateSupported] is true.
     *
     * @return A rotated version of this object.
     */
    fun rotateCounterClockwise(): ScannerBinaryBitmap {
        val newSource = binarizer.luminanceSource.rotateCounterClockwise()
        return ScannerBinaryBitmap(binarizer.createBinarizer(newSource))
    }

    /**
     * Returns a new object with rotated image data by 45 degrees counterclockwise.
     * Only callable if [.isRotateSupported] is true.
     *
     * @return A rotated version of this object.
     */
    fun rotateCounterClockwise45(): ScannerBinaryBitmap {
        val newSource = binarizer.luminanceSource.rotateCounterClockwise45()
        return ScannerBinaryBitmap(binarizer.createBinarizer(newSource))
    }

    override fun toString(): String {
        return try {
            blackMatrix.toString()
        } catch (e: ScannerNotFoundException) {
            ""
        }
    }

    init {
        requireNotNull(binarizer) { "Binarizer must be non-null." }
        this.binarizer = binarizer
    }
}