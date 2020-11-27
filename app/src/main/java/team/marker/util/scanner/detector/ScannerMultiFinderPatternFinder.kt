package team.marker.util.scanner.detector

import com.google.zxing.NotFoundException
import com.google.zxing.common.BitMatrix
import team.marker.util.scanner.common.ScannerResultPoint
import team.marker.util.scanner.common.ScannerResultPointCallback
import team.marker.util.scanner.decoder.ScannerDecodeHintType
import java.io.Serializable
import java.util.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

internal class ScannerMultiFinderPatternFinder : ScannerFinderPatternFinder {
    /**
     * A comparator that orders FinderPatterns by their estimated module size.
     */
    private class ModuleSizeComparator : Comparator<ScannerFinderPattern>, Serializable {
        override fun compare(center1: ScannerFinderPattern, center2: ScannerFinderPattern): Int {
            val value = center2.estimatedModuleSize - center1.estimatedModuleSize
            return if (value < 0.0) -1 else if (value > 0.0) 1 else 0
        }
    }

    constructor(image: BitMatrix?) : super(image!!)
    constructor(image: BitMatrix?, resultPointCallback: ScannerResultPointCallback?) : super(image!!, resultPointCallback)

    @Throws(NotFoundException::class)
    private fun selectMutipleBestPatterns(): Array<Array<ScannerFinderPattern>> {
        val possibleCenters = possibleCenters
        val size = possibleCenters.size
        if (size < 3) throw NotFoundException.getNotFoundInstance()
        if (size == 3) return arrayOf(arrayOf(possibleCenters[0], possibleCenters[1], possibleCenters[2]))
        Collections.sort(possibleCenters, ModuleSizeComparator())

        val results: MutableList<Array<ScannerFinderPattern>> = ArrayList()
        for (i1 in 0 until size - 2) {
            val p1 = possibleCenters[i1] ?: continue
            for (i2 in i1 + 1 until size - 1) {
                val p2 = possibleCenters[i2] ?: continue
                val vModSize12 = (p1.estimatedModuleSize - p2.estimatedModuleSize) / min(p1.estimatedModuleSize, p2.estimatedModuleSize)
                val vModSize12A = Math.abs(p1.estimatedModuleSize - p2.estimatedModuleSize)
                if (vModSize12A > DIFF_MODSIZE_CUTOFF && vModSize12 >= DIFF_MODSIZE_CUTOFF_PERCENT) {
                    break
                }
                for (i3 in i2 + 1 until size) {
                    val p3 = possibleCenters[i3] ?: continue

                    // Compare the expected module sizes; if they are really off, skip
                    val vModSize23 = (p2.estimatedModuleSize - p3.estimatedModuleSize) / min(p2.estimatedModuleSize, p3.estimatedModuleSize)
                    val vModSize23A = abs(p2.estimatedModuleSize - p3.estimatedModuleSize)
                    if (vModSize23A > DIFF_MODSIZE_CUTOFF && vModSize23 >= DIFF_MODSIZE_CUTOFF_PERCENT) break
                    val test = arrayOf(p1, p2, p3)
                    ScannerResultPoint.orderBestPatterns(test as Array<ScannerResultPoint>)

                    // Calculate the distances: a = topleft-bottomleft, b=topleft-topright, c = diagonal
                    val info = ScannerFinderPatternInfo(test)
                    val dA = ScannerResultPoint.distance(info.topLeft, info.bottomLeft)
                    val dC = ScannerResultPoint.distance(info.topRight, info.bottomLeft)
                    val dB = ScannerResultPoint.distance(info.topLeft, info.topRight)

                    // Check the sizes
                    val estimatedModuleCount = (dA + dB) / (p1.estimatedModuleSize * 2.0f)
                    if (estimatedModuleCount > MAX_MODULE_COUNT_PER_EDGE || estimatedModuleCount < MIN_MODULE_COUNT_PER_EDGE) {
                        continue
                    }

                    // Calculate the difference of the edge lengths in percent
                    val vABBC = abs((dA - dB) / Math.min(dA, dB))
                    if (vABBC >= 0.1f) continue

                    // Calculate the diagonal length by assuming a 90° angle at topleft
                    val dCpy = sqrt(dA * dA + dB * dB.toDouble()).toFloat()
                    // Compare to the real distance in %
                    val vPyC = abs((dC - dCpy) / min(dC, dCpy))
                    if (vPyC >= 0.1f) continue
                    results.add(test)
                } // end iterate p3
            } // end iterate p2
        } // end iterate p1
        if (results.isNotEmpty()) return results.toTypedArray()
        throw NotFoundException.getNotFoundInstance()
    }

    @Throws(NotFoundException::class)
    fun findMulti(hints: MutableMap<ScannerDecodeHintType?, Any?>?): Array<ScannerFinderPatternInfo?> {
        val tryHarder = hints != null && hints.containsKey(ScannerDecodeHintType.TRY_HARDER)
        val image = image
        val maxI = image.height
        val maxJ = image.width
        // We are looking for black/white/black/white/black modules in
        // 1:1:3:1:1 ratio; this tracks the number of such modules seen so far

        // Let's assume that the maximum version QR Code we support takes up 1/4 the height of the
        // image, and then account for the center being 3 modules in size. This gives the smallest
        // number of pixels the center could be, so skip this often. When trying harder, look for all
        // QR versions regardless of how dense they are.
        var iSkip = 3 * maxI / (4 * MAX_MODULES)
        if (iSkip < MIN_SKIP || tryHarder) iSkip = MIN_SKIP
        val stateCount = IntArray(5)
        var i = iSkip - 1
        while (i < maxI) {

            // Get a row of black/white values
            clearCounts(stateCount)
            var currentState = 0
            for (j in 0 until maxJ) {
                if (image[j, i]) {
                    // Black pixel
                    if (currentState and 1 == 1) { // Counting white pixels
                        currentState++
                    }
                    stateCount[currentState]++
                } else { // White pixel
                    if (currentState and 1 == 0) { // Counting black pixels
                        if (currentState == 4) { // A winner?
                            if (foundPatternCross(stateCount) && handlePossibleCenter(
                                    stateCount,
                                    i,
                                    j
                                )
                            ) { // Yes
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
            } // for j=...
            if (foundPatternCross(stateCount)) {
                handlePossibleCenter(stateCount, i, maxJ)
            } // end if foundPatternCross
            i += iSkip
        }
        val patternInfo =
            selectMutipleBestPatterns()
        val result: MutableList<ScannerFinderPatternInfo> =
            ArrayList()
        for (pattern in patternInfo) {
            ScannerResultPoint.orderBestPatterns(pattern as Array<ScannerResultPoint>)
            result.add(ScannerFinderPatternInfo(pattern))
        }
        return if (result.isEmpty()) EMPTY_RESULT_ARRAY else result.toTypedArray()
    }

    companion object {
        private val EMPTY_RESULT_ARRAY = arrayOfNulls<ScannerFinderPatternInfo>(0)
        private const val MAX_MODULE_COUNT_PER_EDGE = 180f
        private const val MIN_MODULE_COUNT_PER_EDGE = 9f
        private const val DIFF_MODSIZE_CUTOFF_PERCENT = 0.05f
        private const val DIFF_MODSIZE_CUTOFF = 0.5f
    }
}