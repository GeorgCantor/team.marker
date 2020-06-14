package team.marker.util.scanner.common

import com.google.zxing.Binarizer
import com.google.zxing.BinaryBitmap
import com.google.zxing.NotFoundException
import com.google.zxing.common.BitArray
import com.google.zxing.common.BitMatrix

class ScannerBinaryBitmap(binarizer: Binarizer?) {
    private val binarizer: Binarizer
    private var matrix: BitMatrix? = null

    val width: Int get() = binarizer.width

    val height: Int get() = binarizer.height

    @Throws(NotFoundException::class)
    fun getBlackRow(y: Int, row: BitArray?): BitArray {
        return binarizer.getBlackRow(y, row)
    }

    @get:Throws(NotFoundException::class)
    val blackMatrix: BitMatrix?
        get() {
            if (matrix == null) matrix = binarizer.blackMatrix
            return matrix
        }

    val isCropSupported: Boolean get() = binarizer.luminanceSource.isCropSupported

    fun crop(left: Int, top: Int, width: Int, height: Int): BinaryBitmap {
        val newSource = binarizer.luminanceSource.crop(left, top, width, height)
        return BinaryBitmap(binarizer.createBinarizer(newSource))
    }

    val isRotateSupported: Boolean get() = binarizer.luminanceSource.isRotateSupported

    fun rotateCounterClockwise(): BinaryBitmap {
        val newSource = binarizer.luminanceSource.rotateCounterClockwise()
        return BinaryBitmap(binarizer.createBinarizer(newSource))
    }

    fun rotateCounterClockwise45(): BinaryBitmap {
        val newSource = binarizer.luminanceSource.rotateCounterClockwise45()
        return BinaryBitmap(binarizer.createBinarizer(newSource))
    }

    override fun toString(): String {
        return try {
            blackMatrix.toString()
        } catch (e: NotFoundException) {
            ""
        }
    }

    init {
        requireNotNull(binarizer) { "Binarizer must be non-null." }
        this.binarizer = binarizer
    }
}