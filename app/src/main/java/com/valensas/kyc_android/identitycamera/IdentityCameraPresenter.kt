package com.valensas.kyc_android.identitycamera

import android.content.Context
import android.graphics.Bitmap
import android.media.FaceDetector
import android.util.Log
import com.otaliastudios.cameraview.FrameProcessor
import com.valensas.kyc_android.base.BasePresenter
import com.valensas.kyc_android.identitycamera.model.*
import com.valensas.kyc_android.identitycamera.model.document.DriversLicence
import com.valensas.kyc_android.identitycamera.view.IdentityCameraView

/**
 * Created by Zahit on 17-Jul-18.
 */
class IdentityCameraPresenter : BasePresenter<IdentityCameraView> {
    private var identityCameraView: IdentityCameraView? = null
    private var qrReader = FirebaseQRReader(this)
    private var faceDetector = FirebaseFaceDetection(this)
    private var abbyyOCR = AbbyyOCR(this)


    private var frontFaceScanProcessor: FrameProcessor? = null
    private var frontTextScanProcessor: FrameProcessor? = null
    private var frontFaceScanCompleted = false
    private var frontTextScanCompleted = false
    private var driversLicence: DriversLicence? = null
    private var faceBitmap: Bitmap? = null

    override fun attach(view: IdentityCameraView) {
        identityCameraView = view

    }

    override fun detach() {
        identityCameraView = null
    }

    fun listenFrontIdentityScan() {
        var firstTime = true
        faceDetector.detectionMode = FirebaseFaceDetection.DETECT_IN_DOCUMENT


        frontTextScanProcessor = FrameProcessor {
            if (firstTime) {
                println("Initializing recognition with : ${it.size.width} , ${it.size.height} , ${it.rotation}")
                abbyyOCR.createTextCaptureService()
                abbyyOCR.startRecognition(it.size.width, it.size.height, it.rotation)
                firstTime = false
            }
            abbyyOCR.receiveBuffer(it.data)
        }

        frontFaceScanProcessor = FrameProcessor {
            faceDetector.process(it)
        }

        identityCameraView?.getCameraView()?.addFrameProcessor(frontTextScanProcessor)
        identityCameraView?.getCameraView()?.addFrameProcessor(frontFaceScanProcessor)
    }

    fun listenBackIdentityScan() {
        identityCameraView?.getCameraView()?.addFrameProcessor {
            qrReader.process(it)
        }
    }

    fun listenSelfieScan() {
        faceDetector.detectionMode = FirebaseFaceDetection.DETECT_IN_SELFIE

        identityCameraView?.getCameraView()?.addFrameProcessor {
            faceDetector.process(it)
        }
    }

    fun frontFaceScanSuccessful(faceBitmap: Bitmap) {
        this.frontFaceScanCompleted = true
        this.faceBitmap = faceBitmap
        identityCameraView?.getCameraView()?.removeFrameProcessor(frontFaceScanProcessor)
        faceDetector.firebaseFaceWrapper.faceDetector.close()
        checkIfFrontScanIsCompleted()
    }

    fun frontTextScanSuccessful(driversLicence: DriversLicence) {
        this.frontTextScanCompleted = true
        this.driversLicence = driversLicence
        identityCameraView?.getCameraView()?.removeFrameProcessor(frontTextScanProcessor)
        abbyyOCR.stopRecognition()
        checkIfFrontScanIsCompleted()
    }

    private fun checkIfFrontScanIsCompleted() {

        if (this.frontFaceScanCompleted && this.frontTextScanCompleted) {
            identityCameraView?.getCameraView()?.clearFrameProcessors()

            if (driversLicence != null && faceBitmap != null) {
                identityCameraView?.frontScanCompleted(driversLicence!!, faceBitmap!!)
            } else {
                Log.e("Null:", "Something went wrong")
            }
        }
    }

    fun backQRScanSuccessful(result: String?) {
        if (result != null) {
            println(result)
            identityCameraView?.getCameraView()?.clearFrameProcessors()
            identityCameraView?.backScanCompleted()
            qrReader.firebaseQRWrapper.qrDetector.close()
        }
    }

    fun selfieFaceScanSuccessful(faceBitmap: Bitmap) {
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