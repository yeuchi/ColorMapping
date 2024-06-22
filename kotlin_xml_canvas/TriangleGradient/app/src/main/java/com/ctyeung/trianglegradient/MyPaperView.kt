package com.ctyeung.trianglegradient

/**
 * Description:	matrix triangle gradient method
 *
 * ** NOTE:		There is a bug with Firefox, it is rendering only the boundary.
 * So, I am not using blend-mode.
 *
 * Reference:
 * 3x3 matrix:	The Reproduction of Colour - fifth edition, by Dr. R.W.G. Hunt (pg. 765)
 * canvas fill:	http://dev.opera.com/articles/view/html-5-canvas-the-basics/#paths
 */
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red


class MyPaperView(
    context: Context,
    attrs: AttributeSet
) :
    View(context, attrs) {
    private val dotColor = Color.BLUE
    private val lineColor = Color.argb(255, 0, 0, 0)

    // defines paint and canvas
    private var path: Path? = null
    private var points = arrayListOf<PointF>()
    private var bmp: Bitmap? = null
    private var canvasMask: Canvas? = null

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        path = Path()
    }

    /**
     * View draw call
     * - initialize bmp and canvasMask if 1st time
     * - draw point(s) and triangle otherwise
     */
    override fun onDraw(canvas: Canvas) {
        /*
         * For debugging matrix calculation
         * spoints = arrayListOf<PointF>(PointF(42f, 269f), PointF(134f, 131f), PointF(289f, 262f))
         */

        bmp?.let {
            drawKnots(canvas)
            drawTriangle(canvas)
        } ?: run {
            val height = canvas.height
            val width = canvas.width
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bmp?.let {
                canvasMask = Canvas(it)
            }
        }
    }

    /**
     * Draw a circular dot for each point
     */
    private fun drawKnots(canvas: Canvas) {
        val dotPaint = Paint()
        dotPaint.style = Paint.Style.FILL
        dotPaint.color = dotColor
        points.apply {
            for (p in points) {
                // highlight point
                canvas.drawCircle(p.x.toFloat(), p.y.toFloat(), 5f, dotPaint)
            }
        }
    }

    /**
     * Draw line(s) or a triangle
     * - if a triangle, fill it
     */
    private fun drawTriangle(canvas: Canvas) {
        Paint().let { drawPaint ->
            drawPaint.isAntiAlias = true
            drawPaint.strokeWidth = 3f
            drawPaint.style = Paint.Style.FILL_AND_STROKE
            drawPaint.strokeJoin = Paint.Join.ROUND
            drawPaint.strokeCap = Paint.Cap.ROUND
            drawPaint.color = lineColor

            val path = Path()

            when {
                points.size == 3 -> {
                    path.fillType = Path.FillType.EVEN_ODD
                    points[0].apply {
                        path.moveTo(x, y)
                    }

                    points[1].apply {
                        path.lineTo(x, y)
                    }

                    points[2].apply {
                        path.lineTo(x, y)
                    }
                    path.close()
                    canvas.drawPath(path, drawPaint)

                    /* draw on bitmap -- create a mask for fillGradient */
                    canvasMask?.drawPath(path, drawPaint)

                    /* fill above triangle with gradient colors */
                    fillGradientColors(canvas)
                }

                else -> {
                    for (i in 0..points.size - 2) {
                        val p = points[i]
                        val pp = points[i + 1]
                        path.moveTo(p.x.toFloat(), p.y.toFloat())
                        path.lineTo(pp.x.toFloat(), pp.y.toFloat())
                    }
                    canvas.drawPath(path, drawPaint)
                }
            }
        }
    }

    /**
     * Fill triangle with gradient color
     * a. define 3x3 matrix for 3 points boundary colors
     * b. get rectangle containing triangle
     * c. test if pixel is in triangle -> calculate pixel color; else ignore
     */
    private fun fillGradientColors(canvas: Canvas) {
        bmp?.let {
            val mtx = Matrix().create3x3(
                points,       // [in] array of triangle reference positions
                Color.GREEN,    // [in] triangle reference color 0
                Color.RED,  // [in] triangle reference color 1
                Color.BLUE    // [in] triangle reference color 2
            )

            /* Draw transparent bitmap */
            val rect = findRectBoundTriangle()
            for (y in rect.top until rect.bottom) {
                for (x in rect.left until rect.right) {
                    /*
                     * TODO use line formula to determine pixels inside triangle
                     */
                    val pixel = it.getPixel(x, y)
                    pixel.apply {
                        /* if pixel in triangle */
                        if (green == 0 && red == 0 && blue == 0 && alpha == 255) {
                            val b = requantize(mtx[0] * x + mtx[1] * y + mtx[2])
                            val g = requantize(mtx[3] * x + mtx[4] * y + mtx[5])
                            val r = requantize(mtx[6] * x + mtx[7] * y + mtx[8])
                            val color: Int = Color.argb(255, r, g, b)
                            it.setPixel(x, y, color)
                        }
                    }
                }
            }

            /* draw bitmap into canvas */
            canvas.drawBitmap(it, null, Rect(0,0,it.width, it.height), null)
        }
    }

    /**
     * Find Rect that bounds over triangle (3) points
     * - so we don't iterate over other pixels
     */
    private fun findRectBoundTriangle():Rect {
        val upLeft = PointF(10000F, 10000F)
        val btmRight = PointF(0F, 0F)

        points.forEach{
            if(it.x < upLeft.x) {
                upLeft.x = it.x
            }
            if(it.y < upLeft.y) {
                upLeft.y = it.y
            }
            if(it.x > btmRight.x) {
                btmRight.x = it.x
            }
            if(it.y > btmRight.y) {
                btmRight.y = it.y
            }
        }
        return Rect(upLeft.x.toInt(), upLeft.y.toInt(), btmRight.x.toInt(), btmRight.y.toInt())
    }

    /**
     * Clam pixel color value to 0 - 255
     */
    private fun requantize(num: Double): Int {
        return when {
            num < 0 -> 0
            num > 255 -> 255
            else -> num.toInt()
        }
    }

    /**
     * Clear rendering
     */
    fun clear() {
        points.clear()
        bmp?.eraseColor(Color.TRANSPARENT)
//        path = Path()
        postInvalidate() // Indicate view should be redrawn
    }

    /**
     * Touch event -> create point(s)
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val point = PointF(event.x, event.y)

        // Checks for the event that occurs
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
            }

            /*case MotionEvent.ACTION_MOVE:
                path.lineTo(point.x, point.y);
                break;*/

            MotionEvent.ACTION_UP -> {
                points.add(point)
                if(points.size>3) {
                    points.removeAt(0)
                }
                invalidate()
            }

            else -> return false
        }// Starts a new line in the path

        return true
    }
}