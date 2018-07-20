package com.valensas.kyc_android.facedetection

import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Zahit on 16-Jul-18.
 */
class FirebaseTextRecognitionWrapper {

    val shouldProcess = AtomicBoolean(true)

    val textDetector: FirebaseVisionTextDetector by lazy {
        FirebaseVision.getInstance().visionTextDetector
    }

    fun process(image: FirebaseVisionImage,
                onSuccess: (FirebaseVisionText) -> Unit,
                onError: (Exception) -> Unit) {

        if (!shouldProcess.get())
            return

        shouldProcess.set(false)

        textDetector.detectInImage(image)
                .addOnSuccessListener {
                    onSuccess(it)
                    shouldProcess.set(true)
                }
                .addOnFailureListener {
                    onError(it)
                    Log.e(TAG, "Error processing images: $it")
                }
    }


    companion object {
        private val TAG = FirebaseTextRecognitionWrapper::class.java.simpleName
    }
}