package team.marker.util.scanner.common

import com.google.zxing.NotFoundException
import com.google.zxing.common.BitArray
import com.google.zxing.common.BitMatrix
import okhttp3.internal.and

open class ScannerGlobalHistogramBinarizer(source: ScannerLuminanceSource?) :
    ScannerBinarizer(source!!) {
    private var luminances: ByteArray
    private val buckets: IntArray

    // Applies simple sharpening to the row data to improve performance of the 1D Readers.
    @Throws(NotFoundException::class)
    override fun getBlackRow(y: Int, row: BitArray?): BitArray? {
        var row: BitArray? = row
        val source = luminanceSource
        val width = source.width
        if (row == null || row.size < width) {
            row = BitArray(width)
        } else {
            row.clear()
        }
        initArrays(width)
        val localLuminances = source.getRow(y, luminances)
        val localBuckets = buckets
        for (x in 0 until width) {
            localBuckets[localLuminances[x] and 0xff shr LUMINANCE_SHIFT]++
        }
        val blackPoint = estimateBlackPoint(localBuckets)
        if (width < 3) {
            // Special case for very small images
            for (x in 0 until width) {
                if (localLuminances[x] and 0xff < blackPoint) {
                    row.set(x)
                }
            }
        } else {
            var left: Int = localLuminances[0] and 0xff
            var center: Int = localLuminances[1] and 0xff
            for (x in 1 until width - 1) {
                val right: Int = localLuminances[x + 1] and 0xff
                // A simple -1 4 -1 box filter with a weight of 2.
                if ((center * 4 - left - right) / 2 < blackPoint) {
                    row.set(x)
                }
                left = center
                center = right
            }
        }
        return row
    }

    override val blackMatrix: BitMatrix?
        get() = getMatrix()

    // Does not sharpen the data, as this call is intended to only be used by 2D Readers.
    @Throws(NotFoundException::class)
    private fun getMatrix(): BitMatrix {
        val source = luminanceSource
        val width = source.width
        val height = source.height
        val matrix = BitMatrix(width, height)

        // Quickly calculates the histogram by sampling four rows from the image. This proved to be
        // more robust on the blackbox tests than sampling a diagonal as we used to do.
        initArrays(width)
        val localBuckets = buckets
        for (y in 1..4) {
            val row = height * y / 5
            val localLuminances = source.getRow(row, luminances)
            val right = width * 4 / 5
            for (x in width / 5 until right) {
                val pixel: Int = localLuminances[x] and 0xff
                localBuckets[pixel shr LUMINANCE_SHIFT]++
            }
        }
        val blackPoint = estimateBlackPoint(localBuckets)

        // We delay reading the entire image luminance until the black point estimation succeeds.
        // Although we end up reading four rows twice, it is consistent with our motto of
        // "fail quickly" which is necessary for continuous scanning.
        val localLuminances = source.matrix
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                val pixel: Int = localLuminances!![offset + x] and 0xff
                if (pixel < blackPoint) {
                    matrix[x] = y
                }
            }
        }
        return matrix
    }

    override fun createBinarizer(source: ScannerLuminanceSource?): ScannerBinarizer {
        return ScannerGlobalHistogramBinarizer(source)
    }

    private fun initArrays(luminanceSize: Int) {
        if (luminances.size < luminanceSize) {
            luminances = ByteArray(luminanceSize)
        }
        for (x in 0 until LUMINANCE_BUCKETS) {
            buckets[x] = 0
        }
    }

    companion object {
        private const val LUMINANCE_BITS = 5
        private const val LUMINANCE_SHIFT = 8 - LUMINANCE_BITS
        private const val LUMINANCE_BUCKETS = 1 shl LUMINANCE_BITS
        private val EMPTY = ByteArray(0)

        @Throws(NotFoundException::class)
        private fun estimateBlackPoint(buckets: IntArray): Int {
            // Find the tallest peak in the histogram.
            val numBuckets = buckets.size
            var maxBucketCount = 0
            var firstPeak = 0
            var firstPeakSize = 0
            for (x in 0 until numBuckets) {
                if (buckets[x] > firstPeakSize) {
                    firstPeak = x
                    firstPeakSize = buckets[x]
                }
                if (buckets[x] > maxBucketCount) {
                    maxBucketCount = buckets[x]
                }
            }

            // Find the second-tallest peak which is somewhat far from the tallest peak.
            var secondPeak = 0
            var secondPeakScore = 0
            for (x in 0 until numBuckets) {
                val distanceToBiggest = x - firstPeak
                // Encourage more distant second peaks by multiplying by square of distance.
                val score = buckets[x] * distanceToBiggest * distanceToBiggest
                if (score > secondPeakScore) {
                    secondPeak = x
                    secondPeakScore = score
                }
            }

            // Make sure firstPeak corresponds to the black peak.
            if (firstPeak > secondPeak) {
                val temp = firstPeak
                firstPeak = secondPeak
                secondPeak = temp
            }

            // If there is too little contrast in the image to pick a meaningful black point, throw rather
            // than waste time trying to decode the image, and risk false positives.
            if (secondPeak - firstPeak <= numBuckets / 16) {
                throw NotFoundException.getNotFoundInstance()
            }

            // Find a valley between them that is low and closer to the white peak.
            var bestValley = secondPeak - 1
            var bestValleyScore = -1
            for (x in secondPeak - 1 downTo firstPeak + 1) {
                val fromFirst = x - firstPeak
                val score = fromFirst * fromFirst * (secondPeak - x) * (maxBucketCount - buckets[x])
                if (score > bestValleyScore) {
                    bestValley = x
                    bestValleyScore = score
                }
            }
            return bestValley shl LUMINANCE_SHIFT
        }
    }

    init {
        luminances = EMPTY
        buckets = IntArray(LUMINANCE_BUCKETS)
    }
}