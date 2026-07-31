package com.huawei.hms.arengine

import android.content.Context

// Minimal compile-time stub for Huawei AREngineApk used by ArEngineSelector.
// This ensures CI builds succeed. Replace with the real Huawei SDK for runtime behavior.
class AREngineApk {
    enum class Availability {
        SUPPORTED_INSTALLED,
        SUPPORTED_APK_TOO_OLD,
        SUPPORTED_NOT_INSTALLED,
        UNSUPPORTED
    }

    companion object {
        // returns UNSUPPORTED by default; runtime behavior requires the real Huawei SDK
        fun checkAvailability(context: Context): Availability {
            return Availability.UNSUPPORTED
        }
    }
}
