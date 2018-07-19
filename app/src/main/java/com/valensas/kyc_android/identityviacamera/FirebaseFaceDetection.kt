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
import java.io.ByteArrayOutputStream


/**
 * Created by Zahit on 19-Jul-18.
 */
class FirebaseFaceDetection(val identityCameraPresenter: IdentityCameraPresenter?) {

    private val firebaseFaceWrapper = FirebaseFaceWrapper()


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

                            if (face.leftEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY &&
                                    face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY &&
                                    face.boundingBox.left > 0 && face.boundingBox.right < frame.size.width &&
                                    face.boundingBox.top > 0 && face.boundingBox.bottom < frame.size.height) {

                                val out = ByteArrayOutputStream()
                                val yuvImage = YuvImage(frame.data, frame.format, frame.size.width, frame.size.height, null)
                                yuvImage.compressToJpeg(Rect(0, 0, frame.size.width, frame.size.height), 90, out)

                                val imageBytes = out.toByteArray()
                                val frameBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                out.flush()
                                out.close()

                                val box = face.boundingBox
                                println(frameBitmap.width.toString() + "," + frameBitmap.height)
                                val croppedBitmap = Bitmap.createBitmap(frameBitmap, box.left, box.bottom,
                                        box.width(), box.height())
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

    companion object {
        private const val RIGHT_ANGLE = 90
    }

}