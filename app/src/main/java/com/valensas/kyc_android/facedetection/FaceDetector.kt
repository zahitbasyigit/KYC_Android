package com.valensas.kyc_android.facedetection

import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.otaliastudios.cameraview.Frame

/**
 * Created by Zahit on 16-Jul-18.
 */
class FaceDetector(val faceDetectionPresenter: FaceDetectionPresenter?) {

    private val firebaseFaceDetectorWrapper = FirebaseFaceDetectorWrapper()

    fun process(frame: Frame) {
        detectFacesIn(frame)
    }

    private fun detectFacesIn(frame: Frame) {
        frame.data?.let {
            firebaseFaceDetectorWrapper.process(
                    image = convertFrameToImage(frame),
                    onSuccess = {
                        faceDetectionPresenter?.faceDetectionSuccessful(it.size != 0)
                    },
                    onError = {
                        faceDetectionPresenter?.faceDetectionSuccessful(false)
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