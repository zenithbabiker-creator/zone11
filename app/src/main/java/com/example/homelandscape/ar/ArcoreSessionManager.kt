package com.example.homelandscape.ar

import android.content.Context
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException

class ArcoreSessionManager(private val context: Context) {

    private var session: Session? = null

    @Throws(
        UnavailableArcoreNotInstalledException::class,
        UnavailableApkTooOldException::class,
        UnavailableSdkTooOldException::class,
        UnavailableDeviceNotCompatibleException::class,
    )
    fun createSession(): Session {
        val existing = session
        if (existing != null) {
            return existing
        }
        val newSession = Session(context)
        val config = Config(newSession).apply {
            depthMode = Config.DepthMode.AUTOMATIC
            updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            focusMode = Config.FocusMode.AUTO
        }
        newSession.configure(config)
        session = newSession
        return newSession
    }

    fun createDepthProvider(defaultMetersPerPixel: Double = 0.01): ArDepthProvider {
        createSession()
        return ArcoreDepthProvider(defaultMetersPerPixel)
    }

    fun close() {
        session?.close()
        session = null
    }

    private inner class ArcoreDepthProvider(
        private var cachedMetersPerPixel: Double,
    ) : ArDepthProvider {

        override fun referencePlaneHeightMeters(): Float = 0f

        override fun depthSamplesInsidePolygon(normalizedPolygon: List<Pair<Float, Float>>): List<Float> {
            if (normalizedPolygon.isEmpty()) {
                return emptyList()
            }
            return normalizedPolygon.map { (_, y) ->
                0.05f + (y * 0.02f)
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
