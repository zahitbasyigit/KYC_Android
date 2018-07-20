package com.valensas.kyc_android.facedetection

import android.util.Log
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.otaliastudios.cameraview.Frame
import com.valensas.kyc_android.identityviacamera.IdentityCameraActivity
import com.valensas.kyc_android.identityviacamera.IdentityCameraPresenter

/**
 * Created by Zahit on 16-Jul-18.
 */
class FirebaseQRReader(val identityCameraPresenter: IdentityCameraPresenter?) {

    val firebaseQRWrapper = FirebaseQRWrapper()


    fun process(frame: Frame) {
        detectQRIn(frame)
    }

    private fun detectQRIn(frame: Frame) {
        frame.data?.let {
            firebaseQRWrapper.process(
                    image = convertFrameToImage(frame),
                    onSuccess = {
                        Log.d("Scanner", "Scanning Barcodes")
                        if (it.isNotEmpty() && IdentityCameraActivity.flowState == IdentityCameraActivity.state.STATE_BACK_SCAN) {
                            val rawValue = it[0].rawValue
                            identityCameraPresenter?.qrReadSuccessful(rawValue)
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
        private const val RIGHT_ANGLE = 180
    }
}