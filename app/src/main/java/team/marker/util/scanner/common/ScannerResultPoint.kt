package team.marker.util.scanner.common

open class ScannerResultPoint(val x: Float, val y: Float) {

    override fun equals(other: Any?): Boolean {
        if (other is ScannerResultPoint) return x == other.x && y == other.y
        return false
    }

    override fun hashCode(): Int {
        return 31 * java.lang.Float.floatToIntBits(x) + java.lang.Float.floatToIntBits(y)
    }

    override fun toString(): String {
        return "($x,$y)"
    }

    companion object {

        fun orderBestPatterns(patterns: Array<ScannerResultPoint>) {
            val zeroOneDistance: Float = distance(patterns[0], patterns[1])
            val oneTwoDistance: Float = distance(patterns[1], patterns[2])
            val zeroTwoDistance: Float = distance(patterns[0], patterns[2])
            var pointA: ScannerResultPoint?
            val pointB: ScannerResultPoint?
            var pointC: ScannerResultPoint?
            // Assume one closest to other two is B; A and C will just be guesses at first
            if (oneTwoDistance >= zeroOneDistance && oneTwoDistance >= zeroTwoDistance) {
                pointB = patterns[0]
                pointA = patterns[1]
                pointC = patterns[2]
            } else if (zeroTwoDistance >= oneTwoDistance && zeroTwoDistance >= zeroOneDistance) {
                pointB = patterns[1]
                pointA = patterns[0]
                pointC = patterns[2]
            } else {
                pointB = patterns[2]
                pointA = patterns[0]
                pointC = patterns[1]
            }

            // Use cross product to figure out whether A and C are correct or flipped.
            // This asks whether BC x BA has a positive z component, which is the arrangement
            // we want for A, B, C. If it's negative, then we've got it flipped around and
            // should swap A and C.
            if (crossProductZ(pointA, pointB, pointC) < 0.0f) {
                val temp = pointA
                pointA = pointC
                pointC = temp
            }
            patterns[0] = pointA
            patterns[1] = pointB
            patterns[2] = pointC
        }

        private fun distance(pattern1: ScannerResultPoint, pattern2: ScannerResultPoint): Float {
            return ScannerMathUtils.distance(pattern1.x, pattern1.y, pattern2.x, pattern2.y)
        }

        private fun crossProductZ(pointA: ScannerResultPoint, pointB: ScannerResultPoint, pointC: ScannerResultPoint): Float {
            val bX = pointB.x
            val bY = pointB.y
            return (pointC.x - bX) * (pointA.y - bY) - (pointC.y - bY) * (pointA.x - bX)
        }
    }

}