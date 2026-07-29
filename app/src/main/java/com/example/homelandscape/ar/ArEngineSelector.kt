package com.example.homelandscape.ar

import android.content.Context
import android.os.Build
import com.google.ar.core.ArCoreApk
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.huawei.hms.arengine.AREngineApk

object ArEngineSelector {

    fun detect(context: Context): ArBackend {
        if (isHuaweiFamily() && isHuaweiArAvailable(context)) {
            return ArBackend.HUAWEI_ARENGINE
        }
        if (isArcoreAvailable(context)) {
            return ArBackend.ARCORE
        }
        return ArBackend.NONE
    }

    fun displayName(backend: ArBackend): String = when (backend) {
        ArBackend.ARCORE -> "Google ARCore"
        ArBackend.HUAWEI_ARENGINE -> "Huawei AR Engine"
        ArBackend.NONE -> "No AR engine"
    }

    private fun isHuaweiFamily(): Boolean {
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val brand = Build.BRAND.orEmpty()
        return manufacturer.equals("HUAWEI", ignoreCase = true) ||
            brand.equals("HUAWEI", ignoreCase = true) ||
            manufacturer.equals("HONOR", ignoreCase = true)
    }

    private fun isHuaweiArAvailable(context: Context): Boolean {
        return try {
            when (AREngineApk.checkAvailability(context)) {
                AREngineApk.Availability.SUPPORTED_INSTALLED,
                AREngineApk.Availability.SUPPORTED_APK_TOO_OLD,
                AREngineApk.Availability.SUPPORTED_NOT_INSTALLED,
                -> true
                else -> false
            }
        } catch (_: Throwable) {
            false
        }
    }

    private fun isArcoreAvailable(context: Context): Boolean {
        return try {
            when (ArCoreApk.getInstance().checkAvailability(context)) {
                ArCoreApk.Availability.SUPPORTED_INSTALLED,
                ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD,
                ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED,
                -> true
                else -> false
            }
        } catch (_: UnavailableDeviceNotCompatibleException) {
            false
        } catch (_: Throwable) {
            false
        }
    }
}
