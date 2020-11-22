package team.marker.util.scanner.common

/**
 *
 * Encapsulates a point of interest in an image containing a barcode. Typically, this
 * would be the location of a finder pattern or the corner of the barcode, for example.
 *
 * @author Sean Owen
 */
open class ScannerResultPoint(val x: Float, val y: Float) {

    override fun equals(other: Any?): Boolean {
        if (other is ScannerResultPoint) {
            val otherPoint = other
            return x == otherPoint.x && y == otherPoint.y
        }
        return false
    }

    override fun hashCode(): Int {
        return 31 * java.lang.Float.floatToIntBits(x) + java.lang.Float.floatToIntBits(y)
    }

    override fun toString(): String {
        return "($x,$y)"
    }

    companion object {
        /**
         * Orders an array of three ResultPoints in an order [A,B,C] such that AB is less than AC
         * and BC is less than AC, and the angle between BC and BA is less than 180 degrees.
         *
         * @param patterns array of three `ResultPoint` to order
         */
        fun orderBestPatterns(patterns: Array<ScannerResultPoint>) {

            // Find distances between pattern centers
            val zeroOneDistance = distance(patterns[0], patterns[1])
            val oneTwoDistance = distance(patterns[1], patterns[2])
            val zeroTwoDistance = distance(patterns[0], patterns[2])
            var pointAScanner: ScannerResultPoint
            val pointBScanner: ScannerResultPoint
            var pointCScanner: ScannerResultPoint
            // Assume one closest to other two is B; A and C will just be guesses at first
            if (oneTwoDistance >= zeroOneDistance && oneTwoDistance >= zeroTwoDistance) {
                pointBScanner = patterns[0]
                pointAScanner = patterns[1]
                pointCScanner = patterns[2]
            } else if (zeroTwoDistance >= oneTwoDistance && zeroTwoDistance >= zeroOneDistance) {
                pointBScanner = patterns[1]
                pointAScanner = patterns[0]
                pointCScanner = patterns[2]
            } else {
                pointBScanner = patterns[2]
                pointAScanner = patterns[0]
                pointCScanner = patterns[1]
            }

            // Use cross product to figure out whether A and C are correct or flipped.
            // This asks whether BC x BA has a positive z component, which is the arrangement
            // we want for A, B, C. If it's negative, then we've got it flipped around and
            // should swap A and C.
            if (crossProductZ(pointAScanner, pointBScanner, pointCScanner) < 0.0f) {
                val temp = pointAScanner
                pointAScanner = pointCScanner
                pointCScanner = temp
            }
            patterns[0] = pointAScanner
            patterns[1] = pointBScanner
            patterns[2] = pointCScanner
        }

        /**
         * @param pattern1 first pattern
         * @param pattern2 second pattern
         * @return distance between two points
         */
        fun distance(pattern1: ScannerResultPoint, pattern2: ScannerResultPoint): Float {
            return ScannerMathUtils.distance(pattern1.x, pattern1.y, pattern2.x, pattern2.y)
        }

        /**
         * Returns the z component of the cross product between vectors BC and BA.
         */
        private fun crossProductZ(
            pointAScanner: ScannerResultPoint,
            pointBScanner: ScannerResultPoint,
            pointCScanner: ScannerResultPoint
        ): Float {
            val bX = pointBScanner.x
            val bY = pointBScanner.y
            return (pointCScanner.x - bX) * (pointAScanner.y - bY) - (pointCScanner.y - bY) * (pointAScanner.x - bX)
        }
    }
}
