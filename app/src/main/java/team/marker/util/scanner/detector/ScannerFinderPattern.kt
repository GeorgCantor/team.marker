package team.marker.util.scanner.detector

import team.marker.util.scanner.common.ScannerResultPoint

/**
 *
 * Encapsulates a finder pattern, which are the three square patterns found in
 * the corners of QR Codes. It also encapsulates a count of similar finder patterns,
 * as a convenience to the finder's bookkeeping.
 *
 * @author Sean Owen
 */
class ScannerFinderPattern private constructor(
    posX: Float,
    posY: Float,
    val estimatedModuleSize: Float,
    val count: Int
) :
    ScannerResultPoint(posX, posY) {

    internal constructor(posX: Float, posY: Float, estimatedModuleSize: Float) : this(
        posX,
        posY,
        estimatedModuleSize,
        1
    ) {
    }
    /*
void incrementCount() {
  this.count++;
}
 */
    /**
     *
     * Determines if this finder pattern "about equals" a finder pattern at the stated
     * position and size -- meaning, it is at nearly the same center with nearly the same size.
     */
    fun aboutEquals(moduleSize: Float, i: Float, j: Float): Boolean {
        if (Math.abs(i - y) <= moduleSize && Math.abs(j - x) <= moduleSize) {
            val moduleSizeDiff = Math.abs(moduleSize - estimatedModuleSize)
            return moduleSizeDiff <= 1.0f || moduleSizeDiff <= estimatedModuleSize
        }
        return false
    }

    /**
     * Combines this object's current estimate of a finder pattern position and module size
     * with a new estimate. It returns a new `FinderPattern` containing a weighted average
     * based on count.
     */
    fun combineEstimate(i: Float, j: Float, newModuleSize: Float): ScannerFinderPattern {
        val combinedCount = count + 1
        val combinedX = (count * x + j) / combinedCount
        val combinedY = (count * y + i) / combinedCount
        val combinedModuleSize = (count * estimatedModuleSize + newModuleSize) / combinedCount
        return ScannerFinderPattern(combinedX, combinedY, combinedModuleSize, combinedCount)
    }
}