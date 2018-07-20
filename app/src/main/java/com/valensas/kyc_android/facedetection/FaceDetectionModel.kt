package com.valensas.kyc_android.facedetection

import android.graphics.Rect

/**
 * Created by Zahit on 16-Jul-18.
 */


data class MyFrame(
        val data: ByteArray?,
        val rotation: Int,
        val size: MySize,
        val format: Int,
        val isCameraFacingBack: Boolean)

data class MySize(val width: Int, val height: Int)
