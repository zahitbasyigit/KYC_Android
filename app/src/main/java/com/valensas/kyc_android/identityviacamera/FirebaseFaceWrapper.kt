package com.valensas.kyc_android.identityviacamera

import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.valensas.kyc_android.facedetection.FirebaseQRWrapper

/**
 * Created by Zahit on 19-Jul-18.
 */
internal class FirebaseFaceWrapper {

    private val faceDetectorOptions: FirebaseVisionFaceDetectorOptions by lazy {
        FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.15f)
                .setTrackingEnabled(true)
                .build()
    }

    private val faceDetector: FirebaseVisionFaceDetector by lazy {
        FirebaseVision.getInstance().getVisionFaceDetector(faceDetectorOptions)
    }

    fun process(image: FirebaseVisionImage,
                onSuccess: (MutableList<FirebaseVisionFace>) -> Unit,
                onError: (Exception) -> Unit) {
        faceDetector.detectInImage(image)
                .addOnSuccessListener {
                    onSuccess(it)
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