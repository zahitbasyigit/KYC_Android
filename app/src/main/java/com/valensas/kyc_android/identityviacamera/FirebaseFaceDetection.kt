package com.valensas.kyc_android.identityviacamera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.valensas.kyc_android.facedetection.FirebaseQRWrapper
import com.valensas.kyc_android.facedetection.Frame
import android.R.attr.data
import android.graphics.Rect
import android.graphics.YuvImage
import com.otaliastudios.cameraview.CameraUtils
import java.io.ByteArrayOutputStream


/**
 * Created by Zahit on 19-Jul-18.
 */
class FirebaseFaceDetection(val identityCameraPresenter: IdentityCameraPresenter?) {

    val firebaseFaceWrapper = FirebaseFaceWrapper()


    fun process(frame: Frame) {
        detectFaceIn(frame)
    }

    private fun detectFaceIn(frame: Frame) {
        frame.data?.let {
            firebaseFaceWrapper.process(
                    image = convertFrameToImage(frame),
                    onSuccess = {
                        Log.d("Scanner", "Scanning Faces")
                        if (it.isNotEmpty() && IdentityCameraActivity.flowState == IdentityCameraActivity.state.STATE_SELFIE_SCAN) {
                            val face = it[0]

                            printBoundingBox(face.boundingBox)
                            printFrameDimensions(frame)

                            if (face.boundingBox.left > 0 && face.boundingBox.right < frame.size.height &&
                                    face.boundingBox.top > 0 && face.boundingBox.bottom < frame.size.width) {

                                val out = ByteArrayOutputStream()
                                val yuvImage = YuvImage(frame.data, frame.format, frame.size.width, frame.size.height, null)
                                yuvImage.compressToJpeg(Rect(0, 0, frame.size.width, frame.size.height), 90, out)


                                val imageBytes = out.toByteArray()
                                val frameBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                out.flush()
                                out.close()

                                printImageDimensions(frameBitmap)
                                convertBox(face.boundingBox, frameBitmap.height, frameBitmap.width, frameBitmap.width, frameBitmap.height)
                                printBoundingBox(face.boundingBox)
                                val croppedBitmap = Bitmap.createBitmap(
                                        frameBitmap,
                                        face.boundingBox.left,
                                        face.boundingBox.top,
                                        face.boundingBox.width(),
                                        face.boundingBox.height()
                                )

                                identityCameraPresenter?.faceDetectionSuccessful(croppedBitmap)


                            }
                        }

                    },
                    onError = {
                        //Nothing
                    })
        }
    }

    private fun convertFrameToImage(frame: Frame) =
            FirebaseVisionImage.fromByteArray(frame.data!!, extractFrameMetadata(frame))

    private fun extractFrameMetadata(frame: Frame): FirebaseVisionImageMetadata =
            FirebaseVisionImageMetadata.Builder()
                    .setWidth(frame.size.width)
                    .setHeight(frame.size.height)
                    .setFormat(frame.format)
                    .setRotation(frame.rotation / RIGHT_ANGLE)
                    .build()


    private fun printBoundingBox(rect: Rect) {
        println("Rectangle : Left:${rect.left} , Right:${rect.right} , Top:${rect.top} , Bottom:${rect.bottom}")
    }

    private fun printImageDimensions(bitmap: Bitmap) {
        println("Image : Width:${bitmap.width} , Height:${bitmap.height}")
    }

    private fun printFrameDimensions(frame: Frame) {
        println("Frame : Width:${frame.size.width} , Height:${frame.size.height}")
    }

    private fun convertBox(rectangle: Rect, currentWidth: Int, currentHeight: Int, goalWidth: Int, goalHeight: Int) {
        rectangle.left = convertFromRangeToRange(rectangle.left, 0, currentWidth, 0, goalWidth)
        rectangle.right = convertFromRangeToRange(rectangle.right, 0, currentWidth, 0, goalWidth)
        rectangle.bottom = convertFromRangeToRange(rectangle.bottom, 0, currentHeight, 0, goalHeight)
        rectangle.top = convertFromRangeToRange(rectangle.top, 0, currentHeight, 0, goalHeight)
    }

    fun convertFromRangeToRange(myValue: Int, minDomain: Int, maxDomain: Int, minRange: Int, maxRange: Int): Int {
        return (((myValue.toDouble() - minDomain.toDouble()) / (maxDomain.toDouble() - minDomain.toDouble())) *
                (maxRange.toDouble() - minRange.toDouble()) + minRange.toDouble()).toInt()
    }

    companion object {
        private const val RIGHT_ANGLE = 90
    }

}