package com.valensas.kyc_android.identityviacamera

import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Zahit on 19-Jul-18.
 */
class FirebaseFaceWrapper {
    val shouldProcess = AtomicBoolean(true)

    val faceDetectorOptions: FirebaseVisionFaceDetectorOptions by lazy {
        FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                .setMinFaceSize(0.15f)
                .setTrackingEnabled(false)
                .build()
    }

    val faceDetector: FirebaseVisionFaceDetector by lazy {
        FirebaseVision.getInstance().getVisionFaceDetector(faceDetectorOptions)
    }

    fun process(image: FirebaseVisionImage,
                onSuccess: (MutableList<FirebaseVisionFace>) -> Unit,
                onError: (Exception) -> Unit) {
        if (!shouldProcess.get())
            return

        shouldProcess.set(false)

        faceDetector.detectInImage(image)
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
        private val TAG = FirebaseFaceWrapper::class.java.simpleName
    }
}