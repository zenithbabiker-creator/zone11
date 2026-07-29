package com.example.homelandscape.measure

import android.graphics.PointF
import kotlin.math.abs

/**
 * Computes polygon area in square meters from image-space vertices and a scale factor.
 */
class PolygonAreaCalculator {

    fun areaMeters(outlinePointsImageSpace: List<PointF>, scaleMetersPerPixel: Double): Double {
        if (outlinePointsImageSpace.size < 3 || scaleMetersPerPixel <= 0.0) {
            return 0.0
        }
        var sum = 0.0
        val n = outlinePointsImageSpace.size
        for (i in 0 until n) {
            val p1 = outlinePointsImageSpace[i]
            val p2 = outlinePointsImageSpace[(i + 1) % n]
            sum += p1.x * p2.y - p2.x * p1.y
        }
        val areaPixels = abs(sum) / 2.0
        val metersPerPixelSq = scaleMetersPerPixel * scaleMetersPerPixel
        return areaPixels * metersPerPixelSq
    }
}
