package team.marker.util.scanner.common

object ScannerMathUtils {

    fun round(d: Float): Int {
        return (d + if (d < 0.0f) -0.5f else 0.5f).toInt()
    }

    fun distance(aX: Float, aY: Float, bX: Float, bY: Float): Float {
        val xDiff = aX - bX
        val yDiff = aY - bY
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff.toDouble()).toFloat()
    }

    fun distance(aX: Int, aY: Int, bX: Int, bY: Int): Float {
        val xDiff = aX - bX
        val yDiff = aY - bY
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff.toDouble()).toFloat()
    }

    fun sum(array: IntArray): Int {
        var count = 0
        for (a in array) {
            count += a
        }
        return count
    }
}