package team.marker.util.scanner

/**
 * General math-related and numeric utility functions.
 */
object MathUtils {
    /**
     * Ends up being a bit faster than [Math.round]. This merely rounds its
     * argument to the nearest int, where x.5 rounds up to x+1. Semantics of this shortcut
     * differ slightly from [Math.round] in that half rounds down for negative
     * values. -2.5 rounds to -3, not -2. For purposes here it makes no difference.
     *
     * @param d real value to round
     * @return nearest `int`
     */
    fun round(d: Float): Int {
        return (d + if (d < 0.0f) -0.5f else 0.5f).toInt()
    }

    /**
     * @param aX point A x coordinate
     * @param aY point A y coordinate
     * @param bX point B x coordinate
     * @param bY point B y coordinate
     * @return Euclidean distance between points A and B
     */
    fun distance(aX: Float, aY: Float, bX: Float, bY: Float): Float {
        val xDiff = aX - bX
        val yDiff = aY - bY
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff.toDouble()).toFloat()
    }

    /**
     * @param aX point A x coordinate
     * @param aY point A y coordinate
     * @param bX point B x coordinate
     * @param bY point B y coordinate
     * @return Euclidean distance between points A and B
     */
    fun distance(aX: Int, aY: Int, bX: Int, bY: Int): Float {
        val xDiff = aX - bX
        val yDiff = aY - bY
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff.toDouble()).toFloat()
    }

    /**
     * @param array values to sum
     * @return sum of values in array
     */
    fun sum(array: IntArray): Int {
        var count = 0
        for (a in array) {
            count += a
        }
        return count
    }
}
