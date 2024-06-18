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
    val dotColor = Color.BLUE
    val lineColor = Color.argb(255, 0, 0, 0)

    // defines paint and canvas
    var drawPaint: Paint? = null
    var path: Path? = null
    var points = arrayListOf<PointF>()
    var bmp: Bitmap? = null


    init {
        isFocusable = true
        isFocusableInTouchMode = true
        path = Path()
    }

    override fun onDraw(canvas: Canvas) {
       // points = arrayListOf<PointF>(PointF(42f, 269f), PointF(134f, 131f), PointF(289f, 262f))

        bmp?.let {
            drawKnots(canvas)
            drawTriangle(canvas)
        } ?: run {
            val height = canvas.height
            val width = canvas.width
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
    }

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

    private fun drawTriangle(canvas: Canvas) {
        drawPaint = Paint()
        drawPaint!!.isAntiAlias = true
        drawPaint!!.strokeWidth = 3f
        drawPaint!!.style = Paint.Style.FILL_AND_STROKE
        drawPaint!!.strokeJoin = Paint.Join.ROUND
        drawPaint!!.strokeCap = Paint.Cap.ROUND
        drawPaint!!.color = lineColor

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
                canvas.drawPath(path, drawPaint!!)
                bmp?.let {
                    val rect = Rect(0, 0, it.width, it.height)
                    canvas.drawBitmap(it, null, rect, null)
                }
                fillGradientColors(canvas)
            }

            else -> {
                for (i in 0..points.size - 2) {
                    val p = points[i]
                    val pp = points[i + 1]
                    path.moveTo(p.x.toFloat(), p.y.toFloat())
                    path.lineTo(pp.x.toFloat(), pp.y.toFloat())
                }
                canvas.drawPath(path, drawPaint!!)
            }
        }
    }

    private fun fillGradientColors(canvas: Canvas) {
        bmp?.let {
            val mtx = createMatrix(
                points,       // [in] array of triangle reference positions
                Color.GREEN,    // [in] triangle reference color 0
                Color.RED,  // [in] triangle reference color 1
                Color.BLUE    // [in] triangle reference color 2
            )

            /* Draw transparent bitmap */
            val rect = Rect(0, 0, width, height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    /* if pixel in triangle */
                    val pixel = it.getPixel(x,y)
//                    pixel.apply {
//                        if(green == 0 && red == 0 && blue == 0 && alpha == 255) {
                            val b = requant(mtx[0] * x + mtx[1] * y + mtx[2])
                            val g = requant(mtx[3] * x + mtx[4] * y + mtx[5])
                            val r = requant(mtx[6] * x + mtx[7] * y + mtx[8])
                            val color: Int = Color.argb(255, r, g, b)
                            it.setPixel(x, y, color)
//                        }
//                    }
                }
            }

            /* draw bitmap into canvas */
            canvas.drawBitmap(it, null, rect, null)
        }
    }

    private fun requant(num: Double): Int {
        return when {
            num < 0 -> 0
            num > 255 -> 255
            else -> num.toInt()
        }
    }

    /**
     * Color encoding
     * https://developer.android.com/reference/kotlin/android/graphics/Color
     */
    private fun createMatrix(
        pointList: ArrayList<PointF>,
        color1: Int,
        color2: Int,
        color3: Int,
    ): ArrayList<Double> {
        /*
         * TODO Use 4x4 matrix
         *  https://developer.android.com/reference/android/opengl/Matrix
         */

        val dMtx = arrayListOf<Double>(
            pointList[0].x.toDouble(), pointList[0].y.toDouble(), 1.0,
            pointList[1].x.toDouble(), pointList[1].y.toDouble(), 1.0,
            pointList[2].x.toDouble(), pointList[2].y.toDouble(), 1.0
        )

        // inverse matrix
        val det = dMtx[0] * (dMtx[4] * dMtx[8] - dMtx[5] * dMtx[7]) - dMtx[1] * (dMtx[3] * dMtx[8] - dMtx[5] * dMtx[6]) + dMtx[2] * (dMtx[3] * dMtx[7] - dMtx[4] * dMtx[6])

        // det = 33442
        var iMtx = arrayListOf<Double>(
            0.0, 0.0, 0.0,
            0.0, 0.0, 0.0,
            0.0, 0.0, 0.0
        )
        iMtx[0] = (dMtx[4] * dMtx[8] - dMtx[5] * dMtx[7]) / det;
        iMtx[3] = (dMtx[6] * dMtx[5] - dMtx[3] * dMtx[8]) / det;
        iMtx[6] = (dMtx[3] * dMtx[7] - dMtx[4] * dMtx[6]) / det;
        iMtx[1] = (dMtx[2] * dMtx[7] - dMtx[1] * dMtx[8]) / det;
        iMtx[4] = (dMtx[0] * dMtx[8] - dMtx[2] * dMtx[6]) / det;
        iMtx[7] = (dMtx[1] * dMtx[6] - dMtx[0] * dMtx[7]) / det;
        iMtx[2] = (dMtx[1] * dMtx[5] - dMtx[2] * dMtx[4]) / det;
        iMtx[5] = (dMtx[2] * dMtx[3] - dMtx[0] * dMtx[5]) / det;
        iMtx[8] = (dMtx[0] * dMtx[4] - dMtx[1] * dMtx[3]) / det;

//        (-0.003917229830751749, -0.0002093176245439866, 0.0041265474552957355,
//        0.004634890257759703, -0.007385921894623527, 0.002751031636863824,
//        -0.08226182644578674, 1.9956043298845763, -0.9133425034387895)

        // color matrix
        val red1 = Color.red(color1)
        val grn1 = Color.green(color1)
        val blu1 = Color.blue(color1)

        val red2 = Color.red(color2)
        val grn2 = Color.green(color2)
        val blu2 = Color.blue(color2)

        val red3 = Color.red(color3)
        val grn3 = Color.green(color3)
        val blu3 = Color.blue(color3)

        // find matrix
        val mtx = arrayListOf<Double>(
            0.0, 0.0, 0.0,
            0.0, 0.0, 0.0,
            0.0, 0.0, 0.0
        )
        mtx[0] = red1 * iMtx[0] + grn1 * iMtx[1] + blu1 * iMtx[2]
        mtx[1] = red1 * iMtx[3] + grn1 * iMtx[4] + blu1 * iMtx[5]
        mtx[2] = red1 * iMtx[6] + grn1 * iMtx[7] + blu1 * iMtx[8]

        mtx[3] = red2 * iMtx[0] + grn2 * iMtx[1] + blu2 * iMtx[2]
        mtx[4] = red2 * iMtx[3] + grn2 * iMtx[4] + blu2 * iMtx[5]
        mtx[5] = red2 * iMtx[6] + grn2 * iMtx[7] + blu2 * iMtx[8]

        mtx[6] = red3 * iMtx[0] + grn3 * iMtx[1] + blu3 * iMtx[2]
        mtx[7] = red3 * iMtx[3] + grn3 * iMtx[4] + blu3 * iMtx[5]
        mtx[8] = red3 * iMtx[6] + grn3 * iMtx[7] + blu3 * iMtx[8]
        return mtx
//       -0.053375994258716586, -1.8834100831289995, 508.8791041205669,
//        -0.998893606841696, 1.1818970157287243, -20.976765743675617,
//        1.0522696011004125, 0.7015130674002751, -232.90233837689132
    }

    fun clear() {
        path = Path()
        postInvalidate() // Indicate view should be redrawn
    }

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
                invalidate()
            }

            else -> return false
        }// Starts a new line in the path

        return true
    }
}