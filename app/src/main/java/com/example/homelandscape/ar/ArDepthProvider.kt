package com.example.homelandscape.ar

/**
 * Abstraction for depth samples used by the measurement engine (meters relative to scene).
 */
interface ArDepthProvider {
    fun referencePlaneHeightMeters(): Float
    fun depthSamplesInsidePolygon(normalizedPolygon: List<Pair<Float, Float>>): List<Float>
    fun metersPerPixel(imageWidthPx: Int): Double
    fun release()
}
