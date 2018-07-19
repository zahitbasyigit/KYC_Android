package com.valensas.kyc_android.facedetection

import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector

/**
 * Created by Zahit on 16-Jul-18.
 */
internal class FirebaseTextRecognitionWrapper {

    private val textDetector: FirebaseVisionTextDetector by lazy {
        FirebaseVision.getInstance().visionTextDetector
    }

    fun process(image: FirebaseVisionImage,
                onSuccess: (FirebaseVisionText) -> Unit,
                onError: (Exception) -> Unit) {
        textDetector.detectInImage(image)
                .addOnSuccessListener {
                    onSuccess(it)
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