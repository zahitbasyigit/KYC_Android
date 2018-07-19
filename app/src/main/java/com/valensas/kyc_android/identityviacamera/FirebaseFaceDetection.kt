package com.valensas.kyc_android.identityviacamera

import android.util.Log
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.valensas.kyc_android.facedetection.FirebaseQRWrapper
import com.valensas.kyc_android.facedetection.Frame

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
                                    face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY)
                                identityCameraPresenter?.faceDetectionSuccessful(true)
                        }
                    },
                    onError = {
                        identityCameraPresenter?.faceDetectionSuccessful(false)
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