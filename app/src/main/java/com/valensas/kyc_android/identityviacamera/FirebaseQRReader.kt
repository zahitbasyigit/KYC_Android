package com.valensas.kyc_android.facedetection

import android.util.Log
import android.widget.Toast
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.valensas.kyc_android.identityviacamera.IdentityCameraActivity
import com.valensas.kyc_android.identityviacamera.IdentityCameraPresenter

/**
 * Created by Zahit on 16-Jul-18.
 */
class FirebaseQRReader(val identityCameraPresenter: IdentityCameraPresenter?) {

    private val firebaseQRWrapper = FirebaseQRWrapper()


    fun process(frame: Frame) {
        if (IdentityCameraActivity.flowState == IdentityCameraActivity.state.STATE_BACK_SCAN)
            detectQRIn(frame)
    }

    private fun detectQRIn(frame: Frame) {
        frame.data?.let {
            firebaseQRWrapper.process(
                    image = convertFrameToImage(frame),
                    onSuccess = {
                        if (it.isNotEmpty()) {
                            val rawValue = it[0].rawValue
                            identityCameraPresenter?.qrReadSuccessful(rawValue, true)
                        }
                    },
                    onError = {
                        identityCameraPresenter?.qrReadSuccessful(null, false)
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
        private const val RIGHT_ANGLE = 180
    }
}