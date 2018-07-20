package com.valensas.kyc_android.identitycamera

import android.graphics.Bitmap
import com.valensas.kyc_android.base.BasePresenter
import com.valensas.kyc_android.identitycamera.model.FirebaseQRReader
import com.valensas.kyc_android.identitycamera.model.FirebaseFaceDetection
import com.valensas.kyc_android.identitycamera.model.FirebaseTextRecognizer
import com.valensas.kyc_android.identitycamera.view.IdentityCameraView

/**
 * Created by Zahit on 17-Jul-18.
 */
class IdentityCameraPresenter : BasePresenter<IdentityCameraView> {
    private var identityCameraView: IdentityCameraView? = null
    private var textRecognizer = FirebaseTextRecognizer(this)
    private var qrReader = FirebaseQRReader(this)
    private var faceDetector = FirebaseFaceDetection(this)

    override fun attach(view: IdentityCameraView) {
        identityCameraView = view

    }

    override fun detach() {
        identityCameraView = null
    }

    fun listenFrontIdentityScan() {
        identityCameraView?.getCameraView()?.addFrameProcessor {
            textRecognizer.process(it)
        }
    }

    fun listenBackIdentityScan() {
        identityCameraView?.getCameraView()?.addFrameProcessor {
            qrReader.process(it)
        }
    }

    fun listenSelfieScan() {
        identityCameraView?.getCameraView()?.addFrameProcessor {
            faceDetector.process(it)
        }
    }

    fun textDetectionSuccessful(text: String) {
        identityCameraView?.getCameraView()?.clearFrameProcessors()
        identityCameraView?.frontScanCompleted()
        textRecognizer.firebaseTextRecognitionWrapper.textDetector.close()

    }

    fun qrReadSuccessful(result: String?) {
        if (result != null) {
            println(result)
            identityCameraView?.getCameraView()?.clearFrameProcessors()
            identityCameraView?.backScanCompleted()
            qrReader.firebaseQRWrapper.qrDetector.close()
        }
    }

    fun faceDetectionSuccessful(faceBitmap: Bitmap) {
        identityCameraView?.getCameraView()?.clearFrameProcessors()
        identityCameraView?.selfieScanCompleted(faceBitmap)
        faceDetector.firebaseFaceWrapper.faceDetector.close()
    }
}