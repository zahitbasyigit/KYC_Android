package com.valensas.kyc_android.facedetection

import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector

/**
 * Created by Zahit on 16-Jul-18.
 */
internal class FirebaseQRWrapper {

    private val qrReaderOptions: FirebaseVisionBarcodeDetectorOptions by lazy {
        FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                .build()
    }

    private val qrDetector: FirebaseVisionBarcodeDetector by lazy {
        FirebaseVision.getInstance().getVisionBarcodeDetector(qrReaderOptions)
    }

    fun process(image: FirebaseVisionImage,
                onSuccess: (MutableList<FirebaseVisionBarcode>) -> Unit,
                onError: (Exception) -> Unit) {
        qrDetector.detectInImage(image)
                .addOnSuccessListener {
                    onSuccess(it)
                }
                .addOnFailureListener {
                    onError(it)
                    Log.e(TAG, "Error processing images: $it")
                }
    }

    companion object {
        private val TAG = FirebaseQRWrapper::class.java.simpleName
    }
}