package team.marker.util.scanner.common

import com.google.zxing.common.BitMatrix
import okhttp3.internal.and

class ScannerHybridBinarizer(source: ScannerLuminanceSource?) : ScannerGlobalHistogramBinarizer(source) {
    private var matrix: BitMatrix? = null

    override val blackMatrix: BitMatrix?
        get() = getMatrix()

    /**
     * Calculates the final BitMatrix once for all requests. This could be called once from the
     * constructor instead, but there are some advantages to doing it lazily, such as making
     * profiling easier, and not doing heavy lifting when callers don't expect it.
     */
    @Throws(ScannerNotFoundException::class)
    fun getMatrix(): BitMatrix {
        if (matrix != null) {
            return matrix as BitMatrix
        }
        val source = luminanceSource
        val width = source.width
        val height = source.height
        matrix = if (width >= MINIMUM_DIMENSION && height >= MINIMUM_DIMENSION) {
            val luminances = source.matrix
            var subWidth = width shr BLOCK_SIZE_POWER
            if (width and BLOCK_SIZE_MASK != 0) {
                subWidth++
            }
            var subHeight = height shr BLOCK_SIZE_POWER
            if (height and BLOCK_SIZE_MASK != 0) {
                subHeight++
            }
            val blackPoints = calculateBlackPoints(luminances!!, subWidth, subHeight, width, height)
            val newMatrix = BitMatrix(width, height)
            calculateThresholdForBlock(
                luminances,
                subWidth,
                subHeight,
                width,
                height,
                blackPoints,
                newMatrix
            )
            newMatrix
        } else {
            // If the image is too small, fall back to the global histogram approach.
            super.blackMatrix
        }
        return matrix!!
    }

    override fun createBinarizer(source: ScannerLuminanceSource?): ScannerBinarizer {
        return ScannerHybridBinarizer(source)
    }

    companion object {
        // This class uses 5x5 blocks to compute local luminance, where each block is 8x8 pixels.
        // So this is the smallest dimension in each axis we can accept.
        private const val BLOCK_SIZE_POWER = 3
        private const val BLOCK_SIZE = 1 shl BLOCK_SIZE_POWER // ...0100...00
        private const val BLOCK_SIZE_MASK = BLOCK_SIZE - 1 // ...0011...11
        private const val MINIMUM_DIMENSION = BLOCK_SIZE * 5
        private const val MIN_DYNAMIC_RANGE = 24

        /**
         * For each block in the image, calculate the average black point using a 5x5 grid
         * of the blocks around it. Also handles the corner cases (fractional blocks are computed based
         * on the last pixels in the row/column which are also used in the previous block).
         */
        private fun calculateThresholdForBlock(
            luminances: ByteArray,
            subWidth: Int,
            subHeight: Int,
            width: Int,
            height: Int,
            blackPoints: Array<IntArray>,
            matrix: BitMatrix
        ) {
            val maxYOffset = height - BLOCK_SIZE
            val maxXOffset = width - BLOCK_SIZE
            for (y in 0 until subHeight) {
                var yoffset = y shl BLOCK_SIZE_POWER
                if (yoffset > maxYOffset) {
                    yoffset = maxYOffset
                }
                val top = cap(y, 2, subHeight - 3)
                for (x in 0 until subWidth) {
                    var xoffset = x shl BLOCK_SIZE_POWER
                    if (xoffset > maxXOffset) {
                        xoffset = maxXOffset
                    }
                    val left = cap(x, 2, subWidth - 3)
                    var sum = 0
                    for (z in -2..2) {
                        val blackRow = blackPoints[top + z]
                        sum += blackRow[left - 2] + blackRow[left - 1] + blackRow[left] + blackRow[left + 1] + blackRow[left + 2]
                    }
                    val average = sum / 25
                    thresholdBlock(luminances, xoffset, yoffset, average, width, matrix)
                }
            }
        }

        private fun cap(value: Int, min: Int, max: Int): Int {
            return if (value < min) min else if (value > max) max else value
        }

        /**
         * Applies a single threshold to a block of pixels.
         */
        private fun thresholdBlock(
            luminances: ByteArray,
            xoffset: Int,
            yoffset: Int,
            threshold: Int,
            stride: Int,
            matrix: BitMatrix
        ) {
            var y = 0
            var offset = yoffset * stride + xoffset
            while (y < BLOCK_SIZE) {
                for (x in 0 until BLOCK_SIZE) {
                    // Comparison needs to be <= so that black == 0 pixels are black even if the threshold is 0.
                    if (luminances[offset + x] and 0xFF <= threshold) {
                        matrix[xoffset + x] = yoffset + y
                    }
                }
                y++
                offset += stride
            }
        }

        /**
         * Calculates a single black point for each block of pixels and saves it away.
         * See the following thread for a discussion of this algorithm:
         * http://groups.google.com/group/zxing/browse_thread/thread/d06efa2c35a7ddc0
         */
        private fun calculateBlackPoints(
            luminances: ByteArray,
            subWidth: Int,
            subHeight: Int,
            width: Int,
            height: Int
        ): Array<IntArray> {
            val maxYOffset = height - BLOCK_SIZE
            val maxXOffset = width - BLOCK_SIZE
            val blackPoints = Array(subHeight) {
                IntArray(
                    subWidth
                )
            }
            for (y in 0 until subHeight) {
                var yoffset = y shl BLOCK_SIZE_POWER
                if (yoffset > maxYOffset) {
                    yoffset = maxYOffset
                }
                for (x in 0 until subWidth) {
                    var xoffset = x shl BLOCK_SIZE_POWER
                    if (xoffset > maxXOffset) {
                        xoffset = maxXOffset
                    }
                    var sum = 0
                    var min = 0xFF
                    var max = 0
                    var yy = 0
                    var offset = yoffset * width + xoffset
                    while (yy < BLOCK_SIZE) {
                        for (xx in 0 until BLOCK_SIZE) {
                            val pixel: Int = luminances[offset + xx] and 0xFF
                            sum += pixel
                            // still looking for good contrast
                            if (pixel < min) {
                                min = pixel
                            }
                            if (pixel > max) {
                                max = pixel
                            }
                        }
                        // short-circuit min/max tests once dynamic range is met
                        if (max - min > MIN_DYNAMIC_RANGE) {
                            // finish the rest of the rows quickly
                            yy++
                            offset += width
                            while (yy < BLOCK_SIZE) {
                                for (xx in 0 until BLOCK_SIZE) {
                                    sum += luminances[offset + xx] and 0xFF
                                }
                                yy++
                                offset += width
                            }
                        }
                        yy++
                        offset += width
                    }

                    // The default estimate is the average of the values in the block.
                    var average = sum shr BLOCK_SIZE_POWER * 2
                    if (max - min <= MIN_DYNAMIC_RANGE) {
                        // If variation within the block is low, assume this is a block with only light or only
                        // dark pixels. In that case we do not want to use the average, as it would divide this
                        // low contrast area into black and white pixels, essentially creating data out of noise.
                        //
                        // The default assumption is that the block is light/background. Since no estimate for
                        // the level of dark pixels exists locally, use half the min for the block.
                        average = min / 2
                        if (y > 0 && x > 0) {
                            // Correct the "white background" assumption for blocks that have neighbors by comparing
                            // the pixels in this block to the previously calculated black points. This is based on
                            // the fact that dark barcode symbology is always surrounded by some amount of light
                            // background for which reasonable black point estimates were made. The bp estimated at
                            // the boundaries is used for the interior.

                            // The (min < bp) is arbitrary but works better than other heuristics that were tried.
                            val averageNeighborBlackPoint =
                                (blackPoints[y - 1][x] + 2 * blackPoints[y][x - 1] + blackPoints[y - 1][x - 1]) / 4
                            if (min < averageNeighborBlackPoint) {
                                average = averageNeighborBlackPoint
                            }
                        }
                    }
                    blackPoints[y][x] = average
                }
            }
            return blackPoints
        }
    }
}
