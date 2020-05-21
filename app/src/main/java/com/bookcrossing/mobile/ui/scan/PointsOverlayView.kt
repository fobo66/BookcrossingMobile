/*
 *    Copyright 2020 Andrey Mukamolov
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.bookcrossing.mobile.ui.scan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import com.bookcrossing.mobile.R.styleable
import com.bookcrossing.mobile.util.px

/**
 * Overlay view for QR code scanner to mark scanned code points
 * Created 18.06.17.
 */
class PointsOverlayView : View {
  private var points: Array<Point>? = null
  private val paint = Paint()
  private var pointSize = DEFAULT_POINT_SIZE

  constructor(context: Context?) : super(context) {
    initPaint()
  }

  private fun initPaint() {
    paint.color = Color.YELLOW
    paint.style = FILL
  }

  constructor(context: Context, attrs: AttributeSet?) : super(
    context,
    attrs
  ) {
    initPaint(context, attrs)
  }

  constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
  ) : super(context, attrs, defStyleAttr) {
    initPaint(context, attrs)
  }

  private fun initPaint(
    context: Context,
    attrs: AttributeSet?
  ) {
    if (attrs != null) {
      val typedArrayOfAttributes =
        context.obtainStyledAttributes(attrs, styleable.PointsOverlayView)
      try {
        paint.color = typedArrayOfAttributes.getColor(
          styleable.PointsOverlayView_pointColor,
          Color.YELLOW
        )
        pointSize = typedArrayOfAttributes.getDimension(
          styleable.PointsOverlayView_pointSize,
          DEFAULT_POINT_SIZE
        )
      } finally {
        typedArrayOfAttributes.recycle()
      }
    } else {
      paint.color = Color.YELLOW
    }
    paint.style = FILL
  }

  fun setPoints(points: Array<Point>) {
    this.points = points
    invalidate()
  }

  override fun draw(canvas: Canvas) {
    super.draw(canvas)

    points?.forEach { point ->
      canvas.drawCircle(point.x.px.toFloat(), point.y.px.toFloat(), pointSize, paint)
    }

  }

  companion object {
    private const val DEFAULT_POINT_SIZE = 10f
  }
}