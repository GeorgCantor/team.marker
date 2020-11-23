package team.marker.util.scanner.detector

import com.google.zxing.DecodeHintType
import com.google.zxing.NotFoundException
import com.google.zxing.common.BitMatrix
import team.marker.util.scanner.common.ScannerResultPoint
import team.marker.util.scanner.common.ScannerResultPointCallback
import java.io.Serializable
import java.util.*

/**
 *
 * This class attempts to find finder patterns in a QR Code. Finder patterns are the square
 * markers at three corners of a QR Code.
 *
 *
 * This class is thread-safe but not reentrant. Each thread must allocate its own object.
 *
 * @author Sean Owen
 */
open class ScannerFinderPatternFinder @JvmOverloads constructor(
    protected val image: BitMatrix,
    resultPointCallback: ScannerResultPointCallback? = null
) {
    val possibleCenters: MutableList<ScannerFinderPattern>
    private var hasSkipped = false
    private val crossCheckStateCount: IntArray
    private val resultPointCallback: ScannerResultPointCallback?
    @JvmName("getPossibleCenters1")
    protected fun getPossibleCenters(): List<ScannerFinderPattern> {
        return possibleCenters
    }

    @Throws(NotFoundException::class)
    fun find(hints: MutableMap<DecodeHintType, Any?>?): ScannerFinderPatternInfo {
        val tryHarder = hints != null && hints.containsKey(DecodeHintType.TRY_HARDER)
        val maxI = image.height
        val maxJ = image.width
        // We are looking for black/white/black/white/black modules in
        // 1:1:3:1:1 ratio; this tracks the number of such modules seen so far

        // Let's assume that the maximum version QR Code we support takes up 1/4 the height of the
        // image, and then account for the center being 3 modules in size. This gives the smallest
        // number of pixels the center could be, so skip this often. When trying harder, look for all
        // QR versions regardless of how dense they are.
        var iSkip = 3 * maxI / (4 * MAX_MODULES)
        if (iSkip < MIN_SKIP || tryHarder) {
            iSkip = MIN_SKIP
        }
        var done = false
        val stateCount = IntArray(5)
        var i = iSkip - 1
        while (i < maxI && !done) {

            // Get a row of black/white values
            clearCounts(stateCount)
            var currentState = 0
            var j = 0
            while (j < maxJ) {
                if (image[j, i]) {
                    // Black pixel
                    if (currentState and 1 == 1) { // Counting white pixels
                        currentState++
                    }
                    stateCount[currentState]++
                } else { // White pixel
                    if (currentState and 1 == 0) { // Counting black pixels
                        if (currentState == 4) { // A winner?
                            if (foundPatternCross(stateCount)) { // Yes
                                val confirmed = handlePossibleCenter(stateCount, i, j)
                                if (confirmed) {
                                    // Start examining every other line. Checking each line turned out to be too
                                    // expensive and didn't improve performance.
                                    iSkip = 2
                                    if (hasSkipped) {
                                        done = haveMultiplyConfirmedCenters()
                                    } else {
                                        val rowSkip = findRowSkip()
                                        if (rowSkip > stateCount[2]) {
                                            // Skip rows between row of lower confirmed center
                                            // and top of presumed third confirmed center
                                            // but back up a bit to get a full chance of detecting
                                            // it, entire width of center of finder pattern

                                            // Skip by rowSkip, but back off by stateCount[2] (size of last center
                                            // of pattern we saw) to be conservative, and also back off by iSkip which
                                            // is about to be re-added
                                            i += rowSkip - stateCount[2] - iSkip
                                            j = maxJ - 1
                                        }
                                    }
                                } else {
                                    shiftCounts2(stateCount)
                                    currentState = 3
                                    j++
                                    continue
                                }
                                // Clear state to start looking again
                                currentState = 0
                                clearCounts(stateCount)
                            } else { // No, shift counts back by two
                                shiftCounts2(stateCount)
                                currentState = 3
                            }
                        } else {
                            stateCount[++currentState]++
                        }
                    } else { // Counting white pixels
                        stateCount[currentState]++
                    }
                }
                j++
            }
            if (foundPatternCross(stateCount)) {
                val confirmed = handlePossibleCenter(stateCount, i, maxJ)
                if (confirmed) {
                    iSkip = stateCount[0]
                    if (hasSkipped) {
                        // Found a third one
                        done = haveMultiplyConfirmedCenters()
                    }
                }
            }
            i += iSkip
        }
        val patternInfo = selectBestPatterns()
        ScannerResultPoint.orderBestPatterns(patternInfo as Array<ScannerResultPoint>)
        return ScannerFinderPatternInfo(patternInfo)
    }

    private fun getCrossCheckStateCount(): IntArray {
        clearCounts(crossCheckStateCount)
        return crossCheckStateCount
    }

    protected fun clearCounts(counts: IntArray) {
        for (x in counts.indices) {
            counts[x] = 0
        }
    }

    protected fun shiftCounts2(stateCount: IntArray) {
        stateCount[0] = stateCount[2]
        stateCount[1] = stateCount[3]
        stateCount[2] = stateCount[4]
        stateCount[3] = 1
        stateCount[4] = 0
    }

    /**
     * After a vertical and horizontal scan finds a potential finder pattern, this method
     * "cross-cross-cross-checks" by scanning down diagonally through the center of the possible
     * finder pattern to see if the same proportion is detected.
     *
     * @param centerI row where a finder pattern was detected
     * @param centerJ center of the section that appears to cross a finder pattern
     * @return true if proportions are withing expected limits
     */
    private fun crossCheckDiagonal(centerI: Int, centerJ: Int): Boolean {
        val stateCount = getCrossCheckStateCount()

        // Start counting up, left from center finding black center mass
        var i = 0
        while (centerI >= i && centerJ >= i && image[centerJ - i, centerI - i]) {
            stateCount[2]++
            i++
        }
        if (stateCount[2] == 0) {
            return false
        }

        // Continue up, left finding white space
        while (centerI >= i && centerJ >= i && !image[centerJ - i, centerI - i]) {
            stateCount[1]++
            i++
        }
        if (stateCount[1] == 0) {
            return false
        }

        // Continue up, left finding black border
        while (centerI >= i && centerJ >= i && image[centerJ - i, centerI - i]) {
            stateCount[0]++
            i++
        }
        if (stateCount[0] == 0) {
            return false
        }
        val maxI = image.height
        val maxJ = image.width

        // Now also count down, right from center
        i = 1
        while (centerI + i < maxI && centerJ + i < maxJ && image[centerJ + i, centerI + i]) {
            stateCount[2]++
            i++
        }
        while (centerI + i < maxI && centerJ + i < maxJ && !image[centerJ + i, centerI + i]) {
            stateCount[3]++
            i++
        }
        if (stateCount[3] == 0) {
            return false
        }
        while (centerI + i < maxI && centerJ + i < maxJ && image[centerJ + i, centerI + i]) {
            stateCount[4]++
            i++
        }
        return if (stateCount[4] == 0) {
            false
        } else foundPatternDiagonal(stateCount)
    }

    /**
     *
     * After a horizontal scan finds a potential finder pattern, this method
     * "cross-checks" by scanning down vertically through the center of the possible
     * finder pattern to see if the same proportion is detected.
     *
     * @param startI row where a finder pattern was detected
     * @param centerJ center of the section that appears to cross a finder pattern
     * @param maxCount maximum reasonable number of modules that should be
     * observed in any reading state, based on the results of the horizontal scan
     * @return vertical center of finder pattern, or [Float.NaN] if not found
     */
    private fun crossCheckVertical(
        startI: Int, centerJ: Int, maxCount: Int,
        originalStateCountTotal: Int
    ): Float {
        val image = image
        val maxI = image.height
        val stateCount = getCrossCheckStateCount()

        // Start counting up from center
        var i = startI
        while (i >= 0 && image[centerJ, i]) {
            stateCount[2]++
            i--
        }
        if (i < 0) {
            return Float.NaN
        }
        while (i >= 0 && !image[centerJ, i] && stateCount[1] <= maxCount) {
            stateCount[1]++
            i--
        }
        // If already too many modules in this state or ran off the edge:
        if (i < 0 || stateCount[1] > maxCount) {
            return Float.NaN
        }
        while (i >= 0 && image[centerJ, i] && stateCount[0] <= maxCount) {
            stateCount[0]++
            i--
        }
        if (stateCount[0] > maxCount) {
            return Float.NaN
        }

        // Now also count down from center
        i = startI + 1
        while (i < maxI && image[centerJ, i]) {
            stateCount[2]++
            i++
        }
        if (i == maxI) {
            return Float.NaN
        }
        while (i < maxI && !image[centerJ, i] && stateCount[3] < maxCount) {
            stateCount[3]++
            i++
        }
        if (i == maxI || stateCount[3] >= maxCount) {
            return Float.NaN
        }
        while (i < maxI && image[centerJ, i] && stateCount[4] < maxCount) {
            stateCount[4]++
            i++
        }
        if (stateCount[4] >= maxCount) {
            return Float.NaN
        }

        // If we found a finder-pattern-like section, but its size is more than 40% different than
        // the original, assume it's a false positive
        val stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2] + stateCount[3] +
                stateCount[4]
        if (5 * Math.abs(stateCountTotal - originalStateCountTotal) >= 2 * originalStateCountTotal) {
            return Float.NaN
        }
        return if (foundPatternCross(stateCount)) centerFromEnd(stateCount, i) else Float.NaN
    }

    /**
     *
     * Like [.crossCheckVertical], and in fact is basically identical,
     * except it reads horizontally instead of vertically. This is used to cross-cross
     * check a vertical cross check and locate the real center of the alignment pattern.
     */
    private fun crossCheckHorizontal(
        startJ: Int, centerI: Int, maxCount: Int,
        originalStateCountTotal: Int
    ): Float {
        val image = image
        val maxJ = image.width
        val stateCount = getCrossCheckStateCount()
        var j = startJ
        while (j >= 0 && image[j, centerI]) {
            stateCount[2]++
            j--
        }
        if (j < 0) {
            return Float.NaN
        }
        while (j >= 0 && !image[j, centerI] && stateCount[1] <= maxCount) {
            stateCount[1]++
            j--
        }
        if (j < 0 || stateCount[1] > maxCount) {
            return Float.NaN
        }
        while (j >= 0 && image[j, centerI] && stateCount[0] <= maxCount) {
            stateCount[0]++
            j--
        }
        if (stateCount[0] > maxCount) {
            return Float.NaN
        }
        j = startJ + 1
        while (j < maxJ && image[j, centerI]) {
            stateCount[2]++
            j++
        }
        if (j == maxJ) {
            return Float.NaN
        }
        while (j < maxJ && !image[j, centerI] && stateCount[3] < maxCount) {
            stateCount[3]++
            j++
        }
        if (j == maxJ || stateCount[3] >= maxCount) {
            return Float.NaN
        }
        while (j < maxJ && image[j, centerI] && stateCount[4] < maxCount) {
            stateCount[4]++
            j++
        }
        if (stateCount[4] >= maxCount) {
            return Float.NaN
        }

        // If we found a finder-pattern-like section, but its size is significantly different than
        // the original, assume it's a false positive
        val stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2] + stateCount[3] +
                stateCount[4]
        if (5 * Math.abs(stateCountTotal - originalStateCountTotal) >= originalStateCountTotal) {
            return Float.NaN
        }
        return if (foundPatternCross(stateCount)) centerFromEnd(stateCount, j) else Float.NaN
    }

    /**
     * @param stateCount reading state module counts from horizontal scan
     * @param i row where finder pattern may be found
     * @param j end of possible finder pattern in row
     * @param pureBarcode ignored
     * @return true if a finder pattern candidate was found this time
     * @see .handlePossibleCenter
     */
    @Deprecated(
        """only exists for backwards compatibility
    """
    )
    protected fun handlePossibleCenter(
        stateCount: IntArray,
        i: Int,
        j: Int,
        pureBarcode: Boolean
    ): Boolean {
        return handlePossibleCenter(stateCount, i, j)
    }

    /**
     *
     * This is called when a horizontal scan finds a possible alignment pattern. It will
     * cross check with a vertical scan, and if successful, will, ah, cross-cross-check
     * with another horizontal scan. This is needed primarily to locate the real horizontal
     * center of the pattern in cases of extreme skew.
     * And then we cross-cross-cross check with another diagonal scan.
     *
     *
     * If that succeeds the finder pattern location is added to a list that tracks
     * the number of times each location has been nearly-matched as a finder pattern.
     * Each additional find is more evidence that the location is in fact a finder
     * pattern center
     *
     * @param stateCount reading state module counts from horizontal scan
     * @param i row where finder pattern may be found
     * @param j end of possible finder pattern in row
     * @return true if a finder pattern candidate was found this time
     */
    protected fun handlePossibleCenter(stateCount: IntArray, i: Int, j: Int): Boolean {
        val stateCountTotal = stateCount[0] + stateCount[1] + stateCount[2] + stateCount[3] +
                stateCount[4]
        var centerJ = centerFromEnd(stateCount, j)
        val centerI = crossCheckVertical(
            i,
            centerJ.toInt(), stateCount[2], stateCountTotal
        )
        if (!java.lang.Float.isNaN(centerI)) {
            // Re-cross check
            centerJ = crossCheckHorizontal(
                centerJ.toInt(),
                centerI.toInt(), stateCount[2], stateCountTotal
            )
            if (!java.lang.Float.isNaN(centerJ) && crossCheckDiagonal(
                    centerI.toInt(),
                    centerJ.toInt()
                )
            ) {
                val estimatedModuleSize = stateCountTotal / 7.0f
                var found = false
                for (index in possibleCenters.indices) {
                    val center = possibleCenters[index]
                    // Look for about the same center and module size:
                    if (center.aboutEquals(estimatedModuleSize, centerI, centerJ)) {
                        possibleCenters[index] =
                            center.combineEstimate(centerI, centerJ, estimatedModuleSize)
                        found = true
                        break
                    }
                }
                if (!found) {
                    val point = ScannerFinderPattern(centerJ, centerI, estimatedModuleSize)
                    possibleCenters.add(point)
                    resultPointCallback?.foundPossibleResultPoint(point)
                }
                return true
            }
        }
        return false
    }

    /**
     * @return number of rows we could safely skip during scanning, based on the first
     * two finder patterns that have been located. In some cases their position will
     * allow us to infer that the third pattern must lie below a certain point farther
     * down in the image.
     */
    private fun findRowSkip(): Int {
        val max = possibleCenters.size
        if (max <= 1) {
            return 0
        }
        var firstConfirmedCenter: ScannerResultPoint? = null
        for (center in possibleCenters) {
            if (center.count >= CENTER_QUORUM) {
                if (firstConfirmedCenter == null) {
                    firstConfirmedCenter = center
                } else {
                    // We have two confirmed centers
                    // How far down can we skip before resuming looking for the next
                    // pattern? In the worst case, only the difference between the
                    // difference in the x / y coordinates of the two centers.
                    // This is the case where you find top left last.
                    hasSkipped = true
                    return (Math.abs(firstConfirmedCenter.x - center.x) -
                            Math.abs(firstConfirmedCenter.y - center.y)).toInt() / 2
                }
            }
        }
        return 0
    }

    /**
     * @return true iff we have found at least 3 finder patterns that have been detected
     * at least [.CENTER_QUORUM] times each, and, the estimated module size of the
     * candidates is "pretty similar"
     */
    private fun haveMultiplyConfirmedCenters(): Boolean {
        var confirmedCount = 0
        var totalModuleSize = 0.0f
        val max = possibleCenters.size
        for (pattern in possibleCenters) {
            if (pattern.count >= CENTER_QUORUM) {
                confirmedCount++
                totalModuleSize += pattern.estimatedModuleSize
            }
        }
        if (confirmedCount < 3) {
            return false
        }
        // OK, we have at least 3 confirmed centers, but, it's possible that one is a "false positive"
        // and that we need to keep looking. We detect this by asking if the estimated module sizes
        // vary too much. We arbitrarily say that when the total deviation from average exceeds
        // 5% of the total module size estimates, it's too much.
        val average = totalModuleSize / max
        var totalDeviation = 0.0f
        for (pattern in possibleCenters) {
            totalDeviation += Math.abs(pattern.estimatedModuleSize - average)
        }
        return totalDeviation <= 0.05f * totalModuleSize
    }

    @Throws(NotFoundException::class)
    private fun selectBestPatterns(): Array<ScannerFinderPattern> {
        val startSize = possibleCenters.size
        if (startSize < 3) {
            // Couldn't find enough finder patterns
            throw NotFoundException.getNotFoundInstance()
        }

        // Filter outlier possibilities whose module size is too different
        if (startSize > 3) {
            // But we can only afford to do so if we have at least 4 possibilities to choose from
            var totalModuleSize = 0.0f
            var square = 0.0f
            for (center in possibleCenters) {
                val size = center.estimatedModuleSize
                totalModuleSize += size
                square += size * size
            }
            val average = totalModuleSize / startSize
            val stdDev =
                Math.sqrt(square / startSize - average * average.toDouble()).toFloat()
            Collections.sort(possibleCenters, FurthestFromAverageComparator(average))
            val limit = Math.max(0.2f * average, stdDev)
            var i = 0
            while (i < possibleCenters.size && possibleCenters.size > 3) {
                val pattern = possibleCenters[i]
                if (Math.abs(pattern.estimatedModuleSize - average) > limit) {
                    possibleCenters.removeAt(i)
                    i--
                }
                i++
            }
        }
        if (possibleCenters.size > 3) {
            // Throw away all but those first size candidate points we found.
            var totalModuleSize = 0.0f
            for (possibleCenter in possibleCenters) {
                totalModuleSize += possibleCenter.estimatedModuleSize
            }
            val average = totalModuleSize / possibleCenters.size
            Collections.sort(possibleCenters, CenterComparator(average))
            possibleCenters.subList(3, possibleCenters.size).clear()
        }
        return arrayOf(
            possibleCenters[0],
            possibleCenters[1],
            possibleCenters[2]
        )
    }

    /**
     *
     * Orders by furthest from average
     */
    private class FurthestFromAverageComparator(private val average: Float) :
        Comparator<ScannerFinderPattern?>, Serializable {
        override fun compare(center1: ScannerFinderPattern?, center2: ScannerFinderPattern?): Int {
            return java.lang.Float.compare(
                Math.abs(center2!!.estimatedModuleSize - average),
                Math.abs(center1!!.estimatedModuleSize - average)
            )
        }
    }

    private class CenterComparator(private val average: Float) :
        Comparator<ScannerFinderPattern?>, Serializable {
        override fun compare(center1: ScannerFinderPattern?, center2: ScannerFinderPattern?): Int {
            val countCompare = Integer.compare(center2!!.count, center1!!.count)
            return if (countCompare == 0) {
                java.lang.Float.compare(
                    Math.abs(center1.estimatedModuleSize - average),
                    Math.abs(center2.estimatedModuleSize - average)
                )
            } else countCompare
        }
    }

    companion object {
        private const val CENTER_QUORUM = 2
        const val MIN_SKIP = 3 // 1 pixel/module times 3 modules/center
        const val MAX_MODULES = 97 // support up to version 20 for mobile clients

        /**
         * Given a count of black/white/black/white/black pixels just seen and an end position,
         * figures the location of the center of this run.
         */
        private fun centerFromEnd(stateCount: IntArray, end: Int): Float {
            return end - stateCount[4] - stateCount[3] - stateCount[2] / 2.0f
        }

        /**
         * @param stateCount count of black/white/black/white/black pixels just read
         * @return true iff the proportions of the counts is close enough to the 1/1/3/1/1 ratios
         * used by finder patterns to be considered a match
         */
        fun foundPatternCross(stateCount: IntArray): Boolean {
            var totalModuleSize = 0
            for (i in 0..4) {
                val count = stateCount[i]
                if (count == 0) {
                    return false
                }
                totalModuleSize += count
            }
            if (totalModuleSize < 7) {
                return false
            }
            val moduleSize = totalModuleSize / 7.0f
            val maxVariance = moduleSize / 2.0f
            // Allow less than 50% variance from 1-1-3-1-1 proportions
            return Math.abs(moduleSize - stateCount[0]) < maxVariance && Math.abs(moduleSize - stateCount[1]) < maxVariance && Math.abs(
                3.0f * moduleSize - stateCount[2]
            ) < 3 * maxVariance && Math.abs(moduleSize - stateCount[3]) < maxVariance && Math.abs(
                moduleSize - stateCount[4]
            ) < maxVariance
        }

        /**
         * @param stateCount count of black/white/black/white/black pixels just read
         * @return true iff the proportions of the counts is close enough to the 1/1/3/1/1 ratios
         * used by finder patterns to be considered a match
         */
        protected fun foundPatternDiagonal(stateCount: IntArray): Boolean {
            var totalModuleSize = 0
            for (i in 0..4) {
                val count = stateCount[i]
                if (count == 0) {
                    return false
                }
                totalModuleSize += count
            }
            if (totalModuleSize < 7) {
                return false
            }
            val moduleSize = totalModuleSize / 7.0f
            val maxVariance = moduleSize / 1.333f
            // Allow less than 75% variance from 1-1-3-1-1 proportions
            return Math.abs(moduleSize - stateCount[0]) < maxVariance && Math.abs(moduleSize - stateCount[1]) < maxVariance && Math.abs(
                3.0f * moduleSize - stateCount[2]
            ) < 3 * maxVariance && Math.abs(moduleSize - stateCount[3]) < maxVariance && Math.abs(
                moduleSize - stateCount[4]
            ) < maxVariance
        }
    }

    /**
     *
     * Creates a finder that will search the image for three finder patterns.
     *
     * @param image image to search
     */
    init {
        possibleCenters = ArrayList()
        crossCheckStateCount = IntArray(5)
        this.resultPointCallback = resultPointCallback
    }
}