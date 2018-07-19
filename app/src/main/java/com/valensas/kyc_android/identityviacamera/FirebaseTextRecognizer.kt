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
class FirebaseTextRecognizer(val identityCameraPresenter: IdentityCameraPresenter?) {

    private val firebaseTextRecognitionWrapper = FirebaseTextRecognitionWrapper()


    fun process(frame: Frame) {
        if (IdentityCameraActivity.flowState == IdentityCameraActivity.state.STATE_FRONT_SCAN)
            detectTextsIn(frame)
    }

    private fun detectTextsIn(frame: Frame) {
        frame.data?.let {
            firebaseTextRecognitionWrapper.process(
                    image = convertFrameToImage(frame),
                    onSuccess = {
                        println("PROCESSING...")
                        /*
                        for (block in it.blocks)
                            for (lines in block.lines)
                                for (elements in lines.elements)
                                    Log.d("Processor", elements.text)
                        */
                        if (true)
                            identityCameraPresenter?.textDetectionSuccessful(it.toString(), true)
                        else
                            identityCameraPresenter?.textDetectionSuccessful(it.toString(), false)

                    },
                    onError = {
                        identityCameraPresenter?.textDetectionSuccessful(it.toString(), false)
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