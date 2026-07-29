package com.example.homelandscape.measure

import android.graphics.PointF
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MeasurementEngineTest {

    private val engine = MeasurementEngine()

    @Test
    fun flatSurfaceMode_multipliesAreaBySelectedThickness() {
        val square = listOf(
            PointF(0f, 0f),
            PointF(100f, 0f),
            PointF(100f, 100f),
            PointF(0f, 100f),
        )
        val result = engine.process(
            outlinePointsImageSpace = square,
            scaleMetersPerPixel = 0.01,
            depthSamplesInsidePolygon = listOf(0f, 0f, 0f, 0f),
            referencePlaneHeightMeters = 0f,
            userSelectedThicknessCm = 20,
        )
        assertEquals(MeasurementMode.FLAT_SURFACE, result.mode)
        assertEquals(1.0, result.areaSquareMeters, 0.001)
        assertEquals(0.2, result.volumeCubicMeters, 0.001)
        assertEquals(20, result.selectedThicknessCm)
    }

    @Test
    fun excavationMode_computesAutomaticFillVolume() {
        val triangle = listOf(
            PointF(0f, 0f),
            PointF(200f, 0f),
            PointF(100f, 150f),
        )
        val depthSamples = listOf(0.10f, 0.12f, 0.11f)
        val result = engine.process(
            outlinePointsImageSpace = triangle,
            scaleMetersPerPixel = 0.005,
            depthSamplesInsidePolygon = depthSamples,
            referencePlaneHeightMeters = 0f,
            userSelectedThicknessCm = null,
        )
        assertEquals(MeasurementMode.EXCAVATION_HOLE, result.mode)
        assertNull(result.selectedThicknessCm)
        assertEquals(true, result.volumeCubicMeters > 0.0)
    }

    @Test
    fun thicknessOptions_coverFiveToNinetyFiveCmInStepsOfFive() {
        assertEquals(19, engine.thicknessOptionsCm.size)
        assertEquals(5, engine.thicknessOptionsCm.first())
        assertEquals(95, engine.thicknessOptionsCm.last())
    }
}
