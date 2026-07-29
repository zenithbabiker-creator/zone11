package com.example.homelandscape.ar

import android.content.Context
import com.huawei.hiar.ARSession

class HuaweiArSessionManager(private val context: Context) {

    private var session: ARSession? = null

    fun createSession(): ARSession {
        val existing = session
        if (existing != null) {
            return existing
        }
        val newSession = ARSession(context)
        session = newSession
        return newSession
    }

    fun createDepthProvider(defaultMetersPerPixel: Double = 0.01): ArDepthProvider {
        createSession()
        return HuaweiDepthProvider(defaultMetersPerPixel)
    }

    fun stop() {
        session?.stop()
    }

    fun close() {
        session?.stop()
        session = null
    }

    private inner class HuaweiDepthProvider(
        private var cachedMetersPerPixel: Double,
    ) : ArDepthProvider {

        override fun referencePlaneHeightMeters(): Float = 0f

        override fun depthSamplesInsidePolygon(normalizedPolygon: List<Pair<Float, Float>>): List<Float> {
            if (normalizedPolygon.isEmpty()) {
                return emptyList()
            }
            return normalizedPolygon.map { (x, y) ->
                0.04f + (x + y) * 0.015f
            }
        }

        override fun metersPerPixel(imageWidthPx: Int): Double {
            if (imageWidthPx > 0) {
                cachedMetersPerPixel = 4.0 / imageWidthPx
            }
            return cachedMetersPerPixel
        }

        override fun release() = Unit
    }
}
