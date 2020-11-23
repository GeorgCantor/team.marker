package team.marker.util.scanner.common

import okhttp3.internal.and

class ScannerInvertedLuminanceSource(private val delegate: ScannerLuminanceSource) :
    ScannerLuminanceSource(delegate.width, delegate.height) {
    
    override fun getRow(y: Int, row: ByteArray?): ByteArray {
        var row = row
        row = delegate.getRow(y, byteArrayOf())
        val width = width
        for (i in 0 until width) {
            row[i] = ((255 - (row[i] and 0xFF)).toByte())
        }
        return row
    }

    private fun getMat(): ByteArray {
        val matrix = delegate.matrix
        val length = width * height
        val invertedMatrix = ByteArray(length)
        for (i in 0 until length) {
            invertedMatrix[i] = (255 - (matrix!![i] and 0xFF)) as Byte
        }
        return invertedMatrix
    }

    override val matrix: ByteArray?
        get() = getMat()

    override fun crop(left: Int, top: Int, width: Int, height: Int): ScannerLuminanceSource {
        return ScannerInvertedLuminanceSource(delegate.crop(left, top, width, height)!!)
    }

    override fun invert(): ScannerLuminanceSource {
        return delegate
    }

    override fun rotateCounterClockwise(): ScannerLuminanceSource {
        return ScannerInvertedLuminanceSource(delegate.rotateCounterClockwise()!!)
    }

    override fun rotateCounterClockwise45(): ScannerLuminanceSource {
        return ScannerInvertedLuminanceSource(delegate.rotateCounterClockwise45()!!)
    }
}
