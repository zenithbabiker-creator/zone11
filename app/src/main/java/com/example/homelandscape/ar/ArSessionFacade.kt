package com.example.homelandscape.ar

import android.content.Context

class ArSessionFacade(context: Context) {

    private val appContext = context.applicationContext
    private val backend: ArBackend = ArEngineSelector.detect(appContext)

    private val arcoreManager = ArcoreSessionManager(appContext)
    private val huaweiManager = HuaweiArSessionManager(appContext)

    private var depthProvider: ArDepthProvider? = null

    fun backend(): ArBackend = backend

    fun ensureDepthProvider(): ArDepthProvider {
        depthProvider?.let { return it }
        val provider = when (backend) {
            ArBackend.ARCORE -> arcoreManager.createDepthProvider()
            ArBackend.HUAWEI_ARENGINE -> huaweiManager.createDepthProvider()
            ArBackend.NONE -> FallbackDepthProvider()
        }
        depthProvider = provider
        return provider
    }

    fun release() {
        depthProvider?.release()
        depthProvider = null
        arcoreManager.close()
        huaweiManager.close()
    }

    private class FallbackDepthProvider : ArDepthProvider {
        override fun referencePlaneHeightMeters(): Float = 0f

        override fun depthSamplesInsidePolygon(normalizedPolygon: List<Pair<Float, Float>>): List<Float> =
            normalizedPolygon.map { 0f }

        override fun metersPerPixel(imageWidthPx: Int): Double =
            if (imageWidthPx > 0) 4.0 / imageWidthPx else 0.01

        override fun release() = Unit
    }
}
