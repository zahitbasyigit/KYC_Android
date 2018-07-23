package com.valensas.kyc_android.identitycamera.model

import android.util.Log
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.otaliastudios.cameraview.Frame
import com.valensas.kyc_android.identitycamera.view.IdentityCameraActivity
import com.valensas.kyc_android.identitycamera.IdentityCameraPresenter

/**
 * Created by Zahit on 16-Jul-18.
 */
class FirebaseTextRecognizer(val identityCameraPresenter: IdentityCameraPresenter?) {

    val firebaseTextRecognitionWrapper = FirebaseTextRecognizerWrapper()


    fun process(frame: Frame) {
        detectTextsIn(frame)
    }

    private fun detectTextsIn(frame: Frame) {
        frame.data?.let {
            firebaseTextRecognitionWrapper.process(
                    image = convertFrameToImage(frame),
                    onSuccess = {
                        Log.d("Scanner", "Scanning Texts")
                        if (IdentityCameraActivity.flowState == IdentityCameraActivity.state.STATE_FRONT_SCAN) {
                            /*
                        for (block in it.blocks)
                            for (lines in block.lines)
                                for (elements in lines.elements)
                                    Log.d("Processor", elements.text)
                        */
                            //identityCameraPresenter?.textDetectionSuccessful()
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