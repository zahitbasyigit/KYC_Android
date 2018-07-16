package com.valensas.kyc_android.facedetection

import android.graphics.Rect

/**
 * Created by Zahit on 16-Jul-18.
 */

enum class Orientation {
    ANGLE_0,
    ANGLE_90,
    ANGLE_180,
    ANGLE_270
}


enum class Facing(val value: Int) {
    BACK(0),
    FRONT(1)
}


fun Int.convertToOrientation() = when (this) {
    0 -> Orientation.ANGLE_0
    90 -> Orientation.ANGLE_90
    180 -> Orientation.ANGLE_180
    270 -> Orientation.ANGLE_270
    else -> Orientation.ANGLE_270
}

fun Boolean.convertToFacing() = when (this) {
    true -> Facing.BACK
    false -> Facing.FRONT
}

data class FaceBounds(val id: Int, val box: Rect)

data class Frame(
        val data: ByteArray?,
        val rotation: Int,
        val size: Size,
        val format: Int,
        val isCameraFacingBack: Boolean)

data class Size(val width: Int, val height: Int)