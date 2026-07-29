package com.example.homelandscape.measure

/**
 * Analyzes depth samples to distinguish excavations from flat surfaces and estimate fill volume.
 */
class SurfaceAnalyzer {

    /** Minimum median depression (m) below reference plane to treat region as a hole. */
    var holeDetectionThresholdMeters: Double = 0.02

    /** Fraction of samples that must lie below the reference plane for hole mode. */
    var holeSampleFractionThreshold: Double = 0.35

    fun detectsHoleOrDepression(
        depthSamples: List<Float>,
        referencePlane: Float,
    ): Boolean {
        if (depthSamples.isEmpty()) {
            return false
        }
        val below = depthSamples.count { it > referencePlane + holeDetectionThresholdMeters }
        val fractionBelow = below.toDouble() / depthSamples.size
        if (fractionBelow >= holeSampleFractionThreshold) {
            return true
        }
        val median = depthSamples.sorted()[depthSamples.size / 2]
        return median > referencePlane + holeDetectionThresholdMeters
    }

    /**
     * Integrates depth deficit relative to [referencePlane] over sample points and scales by [areaM2].
     * Each sample represents local depth (m) at a grid cell; average deficit × area ≈ volume.
     */
    fun volumeToFillCubicMeters(
        areaM2: Double,
        depthSamples: List<Float>,
        referencePlane: Float,
    ): Double {
        if (depthSamples.isEmpty() || areaM2 <= 0.0) {
            return 0.0
        }
        val deficits = depthSamples.map { sample ->
            (sample - referencePlane).coerceAtLeast(0f).toDouble()
        }
        val meanDeficit = deficits.average()
        return areaM2 * meanDeficit
    }
}
