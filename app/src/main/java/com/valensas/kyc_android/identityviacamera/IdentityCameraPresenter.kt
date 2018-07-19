package com.valensas.kyc_android.identityviacamera

import android.graphics.Bitmap
import com.otaliastudios.cameraview.Facing
import com.otaliastudios.cameraview.FrameProcessor
import com.valensas.kyc_android.BasePresenter
import com.valensas.kyc_android.facedetection.FirebaseQRReader
import com.valensas.kyc_android.facedetection.Frame
import com.valensas.kyc_android.facedetection.Size
import com.valensas.kyc_android.identityviacamera.IdentityCameraActivity.state.STATE_FRONT_START
import com.valensas.kyc_android.qrreader.QRReaderActivity

/**
 * Created by Zahit on 17-Jul-18.
 */
class IdentityCameraPresenter : BasePresenter<IdentityCameraView> {
    private var identityCameraView: IdentityCameraView? = null
    private var textRecognizer = com.valensas.kyc_android.facedetection.FirebaseTextRecognizer(this)
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
            textRecognizer.process(Frame(
                    data = it.data,
                    rotation = it.rotation,
                    size = Size(it.size.width, it.size.height),
                    format = it.format,
                    isCameraFacingBack = identityCameraView?.getCameraView()?.facing == Facing.BACK))
        }
    }

    fun listenBackIdentityScan() {
        identityCameraView?.getCameraView()?.addFrameProcessor {
            qrReader.process(Frame(
                    data = it.data,
                    rotation = it.rotation,
                    size = Size(it.size.width, it.size.height),
                    format = it.format,
                    isCameraFacingBack = identityCameraView?.getCameraView()?.facing == Facing.BACK))
        }
    }

    fun listenSelfieScan() {
        identityCameraView?.getCameraView()?.addFrameProcessor {
            faceDetector.process(Frame(
                    data = it.data,
                    rotation = it.rotation,
                    size = Size(it.size.width, it.size.height),
                    format = it.format,
                    isCameraFacingBack = identityCameraView?.getCameraView()?.facing == Facing.BACK))
        }
    }

    fun textDetectionSuccessful(text: String) {
        println(text)
        identityCameraView?.getCameraView()?.clearFrameProcessors()
        identityCameraView?.frontScanCompleted()

    }

    fun qrReadSuccessful(result: String?) {
        if (result != null) {
            println(result)
            identityCameraView?.getCameraView()?.clearFrameProcessors()
            identityCameraView?.backScanCompleted()
        }
    }

    fun faceDetectionSuccessful(faceBitmap: Bitmap) {
        identityCameraView?.getCameraView()?.clearFrameProcessors()
        identityCameraView?.selfieScanCompleted(faceBitmap)
    }
}