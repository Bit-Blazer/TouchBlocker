package com.bitblazer.touchblocker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View

internal class CirclePathView(context: Context) : View(context) {
  private val path = Path()
  private val paint = Paint().apply {
    color = 0x80FFFFFF.toInt() // Semi-transparent white
    style = Paint.Style.STROKE
    strokeWidth = 8f
    strokeCap = Paint.Cap.ROUND
    strokeJoin = Paint.Join.ROUND
    isAntiAlias = true
  }
  
  private val pathPoints = mutableListOf<Pair<Float, Float>>()

  fun addPoint(x: Float, y: Float) {
    pathPoints.add(Pair(x, y))
    
    if (pathPoints.size == 1) {
      path.moveTo(x, y)
    } else {
      path.lineTo(x, y)
    }
    
    invalidate()
  }

  fun clearPath() {
    pathPoints.clear()
    path.reset()
    invalidate()
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    canvas.drawPath(path, paint)
  }
}
