package com.example.homelandscape.ui

import android.graphics.Bitmap
import android.graphics.PointF
import android.net.Uri
import android.os.Parcelable
import com.example.homelandscape.measure.MeasurementResult
import kotlinx.parcelize.Parcelize

@Parcelize
data class CaptureResult(
    val snapshotUri: Uri,
    val imageWidth: Int,
    val imageHeight: Int,
    val outlinePoints: List<PointF>,
) : Parcelable

object CaptureContract {
    const val EXTRA_CAPTURE = "extra_capture_result"
    const val EXTRA_MEASUREMENT = "extra_measurement_result"
}
