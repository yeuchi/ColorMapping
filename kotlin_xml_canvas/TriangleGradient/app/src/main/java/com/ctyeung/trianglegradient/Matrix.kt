package com.ctyeung.trianglegradient

import android.graphics.Color
import android.graphics.PointF

// ===============================================================
// Module:		Matrix.js
// Description:	calculate the transform matrix for triangle gradient
// Reference:	Measuring color, Dr. Hunt
// Author:		C.T. Yeung
// ===============================================================

class Matrix {
    /**
     * Color encoding
     * https://developer.android.com/reference/kotlin/android/graphics/Color
     */
    fun create3x3(
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
        val det =
            dMtx[0] * (dMtx[4] * dMtx[8] - dMtx[5] * dMtx[7]) - dMtx[1] * (dMtx[3] * dMtx[8] - dMtx[5] * dMtx[6]) + dMtx[2] * (dMtx[3] * dMtx[7] - dMtx[4] * dMtx[6])

        /*
         * For debugging matrix calculations
         * det = 33442
         */
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

        /*
         * For debugging matrix calculations
         */
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
        /*
         * For debugging matrix calculations
         */
//       -0.053375994258716586, -1.8834100831289995, 508.8791041205669,
//        -0.998893606841696, 1.1818970157287243, -20.976765743675617,
//        1.0522696011004125, 0.7015130674002751, -232.90233837689132
    }
}