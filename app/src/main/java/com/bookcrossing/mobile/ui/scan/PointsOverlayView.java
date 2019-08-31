package com.bookcrossing.mobile.ui.scan;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import com.bookcrossing.mobile.R;

/**
 * (c) 2017 Andrey Mukamolov <fobo66@protonmail.com>
 * Created 18.06.17.
 */

public class PointsOverlayView extends View {

  private static final float DEFAULT_POINT_SIZE = 10;

  PointF[] points;
  private Paint paint;
  private float pointSize = DEFAULT_POINT_SIZE;

  public PointsOverlayView(Context context) {
    super(context);
    init();
  }

  private void init() {
    paint = new Paint();
    paint.setColor(Color.YELLOW);
    paint.setStyle(Paint.Style.FILL);
  }

  public PointsOverlayView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public PointsOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, @Nullable AttributeSet attrs) {
    paint = new Paint();
    if (attrs != null) {
      TypedArray typedArrayOfAttributes =
          context.obtainStyledAttributes(attrs, R.styleable.PointsOverlayView);

      try {
        paint.setColor(typedArrayOfAttributes.getColor(R.styleable.PointsOverlayView_pointColor,
            Color.YELLOW));
        pointSize = typedArrayOfAttributes.getDimension(R.styleable.PointsOverlayView_pointSize,
            DEFAULT_POINT_SIZE);
      } finally {
        typedArrayOfAttributes.recycle();
      }
    } else {
      paint.setColor(Color.YELLOW);
    }
    paint.setStyle(Paint.Style.FILL);
  }

  public void setPoints(PointF[] points) {
    this.points = points;
    invalidate();
  }

  @Override public void draw(Canvas canvas) {
    super.draw(canvas);
    if (points != null) {
      for (PointF point : points) {
        canvas.drawCircle(point.x, point.y, pointSize, paint);
      }
    }
  }
}
