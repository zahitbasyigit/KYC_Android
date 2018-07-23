package com.valensas.kyc_android.identitycamera

import android.content.Context
import android.graphics.Bitmap
import com.valensas.kyc_android.base.BasePresenter
import com.valensas.kyc_android.identitycamera.model.*
import com.valensas.kyc_android.identitycamera.model.document.Document
import com.valensas.kyc_android.identitycamera.model.driverslicence.DriversLicence
import com.valensas.kyc_android.identitycamera.view.IdentityCameraView

/**
 * Created by Zahit on 17-Jul-18.
 */
class IdentityCameraPresenter : BasePresenter<IdentityCameraView> {
    private var identityCameraView: IdentityCameraView? = null
    private var textRecognizer = FirebaseTextRecognizer(this)
    private var qrReader = FirebaseQRReader(this)
    private var faceDetector = FirebaseFaceDetection(this)
    private var abbyyOCR = AbbyyOCR(this)

    override fun attach(view: IdentityCameraView) {
        identityCameraView = view

    }

    override fun detach() {
        identityCameraView = null
    }

    fun listenFrontIdentityScan() {
        var firstTime = true

        identityCameraView?.getCameraView()?.addFrameProcessor {
            //textRecognizer.process(it)
            if (firstTime) {
                println("Initializing recognition with : ${it.size.width} , ${it.size.height} , ${it.rotation}")
                abbyyOCR.createTextCaptureService()
                abbyyOCR.startRecognition(it.size.width, it.size.height, it.rotation)
                firstTime = false
            }
            abbyyOCR.receiveBuffer(it.data)
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

    fun textDetectionSuccessful(driversLicence: DriversLicence) {
        identityCameraView?.getCameraView()?.clearFrameProcessors()
        identityCameraView?.frontScanCompleted(driversLicence)
        //textRecognizer.firebaseTextRecognitionWrapper.textDetector.close()
        abbyyOCR.stopRecognition()
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

    fun getDefaultRotation(): Int? {
        return identityCameraView?.getDefaultRotation()
    }

    fun getContext(): Context? {
        return identityCameraView?.getActivityContext()
    }
}