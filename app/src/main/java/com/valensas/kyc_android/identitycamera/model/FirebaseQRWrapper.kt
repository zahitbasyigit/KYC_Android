package com.valensas.kyc_android.identitycamera.model

import android.util.Log
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Zahit on 16-Jul-18.
 */
class FirebaseQRWrapper {
    val shouldProcess = AtomicBoolean(true)

    val qrReaderOptions: FirebaseVisionBarcodeDetectorOptions by lazy {
        FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                .build()
    }

    val barcodeReaderOptions: FirebaseVisionBarcodeDetectorOptions by lazy {
        FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_CODE_128)
                .build()
    }

    lateinit var qrDetector: FirebaseVisionBarcodeDetector

    fun initQRDetector(mode: Int) {
        when (mode) {
            QR_READER -> {
                qrDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(qrReaderOptions)
            }
            BARCODE_READER -> {
                qrDetector = FirebaseVision.getInstance().getVisionBarcodeDetector(barcodeReaderOptions)
            }
        }
    }

    fun process(image: FirebaseVisionImage,
                onSuccess: (MutableList<FirebaseVisionBarcode>) -> Unit,
                onError: (Exception) -> Unit) {
        if (!shouldProcess.get())
            return

        shouldProcess.set(false)

        qrDetector.detectInImage(image)
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
        private val TAG = FirebaseQRWrapper::class.java.simpleName
        val QR_READER = 0
        val BARCODE_READER = 1
    }
}