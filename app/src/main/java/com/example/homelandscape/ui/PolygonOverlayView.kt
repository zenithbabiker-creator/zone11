package com.example.homelandscape.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot

class PolygonOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val points = mutableListOf<PointF>()
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF4CAF50")
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#334CAF50")
        style = Paint.Style.FILL
    }
    private val vertexPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    var onPolygonChanged: ((List<PointF>) -> Unit)? = null

    fun setPoints(newPoints: List<PointF>) {
        points.clear()
        points.addAll(newPoints)
        invalidate()
        onPolygonChanged?.invoke(points.toList())
    }

    fun getPoints(): List<PointF> = points.toList()

    fun clearPolygon() {
        points.clear()
        invalidate()
        onPolygonChanged?.invoke(emptyList())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val index = findVertexNear(event.x, event.y)
                if (index >= 0) {
                    points[index].set(event.x, event.y)
                } else {
                    points.add(PointF(event.x, event.y))
                }
                invalidate()
                onPolygonChanged?.invoke(points.toList())
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val index = findVertexNear(event.x, event.y, radius = 48f)
                if (index >= 0) {
                    points[index].set(event.x, event.y)
                    invalidate()
                    onPolygonChanged?.invoke(points.toList())
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findVertexNear(x: Float, y: Float, radius: Float = 32f): Int {
        points.forEachIndexed { index, point ->
            if (hypot(point.x - x, point.y - y) <= radius) {
                return index
            }
        }
        return -1
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.size < 2) {
            points.forEach { drawVertex(canvas, it) }
            return
        }
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
            if (points.size >= 3) {
                close()
            }
        }
        if (points.size >= 3) {
            canvas.drawPath(path, fillPaint)
        }
        canvas.drawPath(path, strokePaint)
        points.forEach { drawVertex(canvas, it) }
    }

    private fun drawVertex(canvas: Canvas, point: PointF) {
        canvas.drawCircle(point.x, point.y, 10f, vertexPaint)
        canvas.drawCircle(point.x, point.y, 10f, strokePaint)
    }

    fun renderSnapshotWithOutline(bitmap: Bitmap): Bitmap {
        val copy = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(copy)
        draw(canvas)
        return copy
    }
}
