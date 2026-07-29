package com.example.homelandscape.measure

import android.graphics.PointF
import java.util.Locale

data class MeasurementResult(
    val mode: MeasurementMode,
    val areaSquareMeters: Double,
    val volumeCubicMeters: Double,
    val displayArea: String,
    val displayVolume: String,
    val selectedThicknessCm: Int? = null,
)

class MeasurementEngine(
    private val polygonAreaCalculator: PolygonAreaCalculator = PolygonAreaCalculator(),
    private val surfaceAnalyzer: SurfaceAnalyzer = SurfaceAnalyzer(),
) {

    val thicknessOptionsCm: List<Int> = (5..95 step 5).toList()

    fun process(
        outlinePointsImageSpace: List<PointF>,
        scaleMetersPerPixel: Double,
        depthSamplesInsidePolygon: List<Float>,
        referencePlaneHeightMeters: Float,
        userSelectedThicknessCm: Int?,
    ): MeasurementResult {
        val areaM2 = polygonAreaCalculator.areaMeters(outlinePointsImageSpace, scaleMetersPerPixel)

        val isHole = surfaceAnalyzer.detectsHoleOrDepression(
            depthSamples = depthSamplesInsidePolygon,
            referencePlane = referencePlaneHeightMeters,
        )

        return if (isHole) {
            val volumeM3 = surfaceAnalyzer.volumeToFillCubicMeters(
                areaM2 = areaM2,
                depthSamples = depthSamplesInsidePolygon,
                referencePlane = referencePlaneHeightMeters,
            )
            MeasurementResult(
                mode = MeasurementMode.EXCAVATION_HOLE,
                areaSquareMeters = areaM2,
                volumeCubicMeters = volumeM3,
                displayArea = formatArea(areaM2),
                displayVolume = formatVolume(volumeM3),
                selectedThicknessCm = null,
            )
        } else {
            val thicknessCm = userSelectedThicknessCm ?: thicknessOptionsCm.first()
            require(thicknessCm in thicknessOptionsCm) {
                "Thickness must be one of ${thicknessOptionsCm.joinToString()} cm"
            }
            val thicknessM = thicknessCm / 100.0
            val volumeM3 = areaM2 * thicknessM
            MeasurementResult(
                mode = MeasurementMode.FLAT_SURFACE,
                areaSquareMeters = areaM2,
                volumeCubicMeters = volumeM3,
                displayArea = formatArea(areaM2),
                displayVolume = formatVolume(volumeM3),
                selectedThicknessCm = thicknessCm,
            )
        }
    }

    private fun formatArea(m2: Double): String =
        String.format(Locale.US, "%.2f m²", m2)

    private fun formatVolume(m3: Double): String =
        String.format(Locale.US, "%.2f m³", m3)
}
