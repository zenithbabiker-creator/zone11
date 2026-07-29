package com.example.homelandscape.ui

import android.graphics.PointF
import com.example.homelandscape.ar.ArDepthProvider
import com.example.homelandscape.measure.MeasurementEngine
import com.example.homelandscape.measure.MeasurementMode
import com.example.homelandscape.measure.MeasurementResult

class MeasurementViewModel(
    private val measurementEngine: MeasurementEngine = MeasurementEngine(),
) {

    val thicknessOptionsCm: List<Int> = measurementEngine.thicknessOptionsCm

    fun calculate(
        outlinePoints: List<PointF>,
        imageWidth: Int,
        imageHeight: Int,
        depthProvider: ArDepthProvider,
        selectedThicknessCm: Int?,
        forceFlatMode: Boolean = false,
    ): MeasurementResult {
        require(outlinePoints.size >= 3) { "Outline at least 3 points" }

        val normalized = outlinePoints.map { point ->
            val nx = (point.x / imageWidth).coerceIn(0f, 1f)
            val ny = (point.y / imageHeight).coerceIn(0f, 1f)
            nx to ny
        }

        val depthSamples = if (forceFlatMode) {
            List(normalized.size) { 0f }
        } else {
            depthProvider.depthSamplesInsidePolygon(normalized)
        }

        return measurementEngine.process(
            outlinePointsImageSpace = outlinePoints,
            scaleMetersPerPixel = depthProvider.metersPerPixel(imageWidth),
            depthSamplesInsidePolygon = depthSamples,
            referencePlaneHeightMeters = depthProvider.referencePlaneHeightMeters(),
            userSelectedThicknessCm = selectedThicknessCm,
        )
    }

    fun isExcavationMode(result: MeasurementResult): Boolean =
        result.mode == MeasurementMode.EXCAVATION_HOLE
}
