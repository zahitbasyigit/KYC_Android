package com.valensas.kyc_android.identitycamera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import com.otaliastudios.cameraview.Frame
import com.otaliastudios.cameraview.FrameProcessor
import com.valensas.kyc_android.base.BasePresenter
import com.valensas.kyc_android.identitycamera.model.*
import com.valensas.kyc_android.identitycamera.model.document.DocumentItemSet
import com.valensas.kyc_android.identitycamera.model.document.DocumentItemSet.Type.*
import com.valensas.kyc_android.identitycamera.model.tensorflow.Classifier
import com.valensas.kyc_android.identitycamera.model.tensorflow.TensorFlowImageClassifier
import com.valensas.kyc_android.identitycamera.model.tensorflow.TensorFlowImageClassifier.Companion.INPUT_HEIGHT
import com.valensas.kyc_android.identitycamera.model.tensorflow.TensorFlowImageClassifier.Companion.INPUT_WIDTH
import com.valensas.kyc_android.identitycamera.view.IdentityCameraView
import java.io.ByteArrayOutputStream

/**
 * Created by Zahit on 17-Jul-18.
 */
class IdentityCameraPresenter : BasePresenter<IdentityCameraView> {

    private var identityCameraView: IdentityCameraView? = null
    private var qrReader = FirebaseQRReader(this)
    private var faceDetector = FirebaseFaceDetection(this)
    private var abbyyOCR = AbbyyOCR(this)
    private lateinit var speechRecognition: SpeechRecognition
    private lateinit var classifier: Classifier

    private var frontFaceScanProcessor: FrameProcessor? = null
    private var frontTextScanProcessor: FrameProcessor? = null
    private var frontDocumentClassifier: FrameProcessor? = null
    private var frontFaceScanCompleted = false
    private var frontTextScanCompleted = false


    private var documentItemSet: DocumentItemSet? = null
    private var faceBitmap: Bitmap? = null

    override fun attach(view: IdentityCameraView) {
        identityCameraView = view
        val assets = identityCameraView?.getActivityAssets()
        assets?.let {
            classifier = TensorFlowImageClassifier.create(it)
        }

        speechRecognition = SpeechRecognition(this)

    }

    override fun detach() {
        identityCameraView = null
        speechRecognition.speech.destroy()
    }

    fun listenFrontIdentityScan() {
        var firstTime = true

        frontTextScanProcessor = FrameProcessor {
            if (firstTime) {
                println("Initializing recognition with : ${it.size.width} , ${it.size.height} , ${it.rotation}")
                abbyyOCR.createTextCaptureService()
                abbyyOCR.startRecognition(it.size.width, it.size.height, it.rotation)
                firstTime = false
            }

            abbyyOCR.receiveBuffer(it.data)
        }



        frontDocumentClassifier = FrameProcessor {
            val results = classifier.recognizeImage(convertFrameToBitmap(it))
            processRecognitionResult(results)
        }


        identityCameraView?.getCameraView()?.addFrameProcessor(frontTextScanProcessor)
        identityCameraView?.getCameraView()?.addFrameProcessor(frontDocumentClassifier)
    }

    fun listenFrontFaceScan() {
        faceDetector.detectionMode = FirebaseFaceDetection.DETECT_IN_DOCUMENT

        frontFaceScanProcessor = FrameProcessor {
            faceDetector.process(it)
        }
        identityCameraView?.getCameraView()?.addFrameProcessor(frontFaceScanProcessor)
    }

    fun listenBackIdentityScan() {
        when (documentItemSet?.type) {
            DRIVERS_LICENCE -> {
                qrReader.firebaseQRWrapper.initQRDetector(FirebaseQRWrapper.QR_READER)
            }
            IDENTITY_CARD -> {
                qrReader.firebaseQRWrapper.initQRDetector(FirebaseQRWrapper.BARCODE_READER)
            }
            PASSPORT -> TODO()
            NONE -> TODO()
            null -> TODO()
        }

        identityCameraView?.getCameraView()?.addFrameProcessor {
            qrReader.process(it)
        }
    }

    fun listenSelfieBlinkScan() {
        faceDetector.detectionMode = FirebaseFaceDetection.DETECT_IN_BLINK_SELFIE

        identityCameraView?.getCameraView()?.addFrameProcessor {
            faceDetector.process(it)
        }
    }

    fun listenSelfieScan() {
        faceDetector.detectionMode = FirebaseFaceDetection.DETECT_IN_SELFIE

        identityCameraView?.getCameraView()?.addFrameProcessor {
            faceDetector.process(it)
        }
    }

    fun listenSpeechRecognition() {
        speechRecognition.recognizeSpeech()
    }


    fun frontFaceScanSuccessful(faceBitmap: Bitmap) {
        this.frontFaceScanCompleted = true
        this.faceBitmap = faceBitmap
        identityCameraView?.getCameraView()?.removeFrameProcessor(frontFaceScanProcessor)
        faceDetector.firebaseFaceWrapper.faceDetector.close()
        checkIfFrontScanIsCompleted()
    }


    fun frontTextScanSuccessful(documentItemSet: DocumentItemSet) {
        this.frontTextScanCompleted = true
        this.documentItemSet = documentItemSet
        identityCameraView?.getCameraView()?.removeFrameProcessor(frontTextScanProcessor)
        identityCameraView?.getCameraView()?.removeFrameProcessor(frontDocumentClassifier)
        abbyyOCR.stopRecognition()
        classifier.close()
        checkIfFrontScanIsCompleted()
    }

    private fun checkIfFrontScanIsCompleted() {

        if (this.frontFaceScanCompleted && this.frontTextScanCompleted) {
            identityCameraView?.getCameraView()?.clearFrameProcessors()

            if (documentItemSet != null && faceBitmap != null) {
                identityCameraView?.frontScanCompleted(documentItemSet!!, faceBitmap!!)
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

    fun speechRecognitionTextAvailable(required: String) {
        identityCameraView?.setSpeechRecognitionText(required)
    }

    fun speechRecognitionSuccessful(found: String) {
        identityCameraView?.speechRecognitionCompleted(found)
    }

    fun speechRecognitionUnsuccessful(message: String) {
        identityCameraView?.speechRecognitionFailed(message)
    }

    fun selfieFaceScanSuccessful(faceBitmap: Bitmap) {
        identityCameraView?.getCameraView()?.clearFrameProcessors()
        identityCameraView?.selfieScanCompleted(faceBitmap)
        faceDetector.firebaseFaceWrapper.faceDetector.close()
    }

    fun selfieFaceBlinkScanSuccessful(faceBitmap: Bitmap) {
        identityCameraView?.getCameraView()?.clearFrameProcessors()
        identityCameraView?.selfieBlinkScanCompleted(faceBitmap)
        faceDetector.firebaseFaceWrapper.faceDetector.close()
    }

    fun getDefaultRotation(): Int? {
        return identityCameraView?.getDefaultRotation()
    }

    fun getContext(): Context? {
        return identityCameraView?.getActivityContext()
    }

    private fun convertFrameToBitmap(frame: Frame): Bitmap {
        val out = ByteArrayOutputStream()
        val yuvImage = YuvImage(frame.data, frame.format, frame.size.width, frame.size.height, null)
        yuvImage.compressToJpeg(Rect(0, 0, frame.size.width, frame.size.height), 100, out)

        val imageBytes = out.toByteArray()
        val frameBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        out.flush()
        out.close()

        val scaledBitmap = Bitmap.createScaledBitmap(frameBitmap, INPUT_WIDTH, INPUT_HEIGHT, false)
        //val rotatedBitmap = rotateImage(scaledBitmap)

        return scaledBitmap
    }

    private fun processRecognitionResult(result: List<Classifier.Recognition>?) {
        if (result != null && result.isEmpty())
            return

        setDocumentType(result?.get(0)?.title ?: "")
        println(result?.get(0)?.title + " " + result?.get(0)?.confidence)


    }

    fun setDeviceIsUpright(isUpright: Boolean) {
        faceDetector.deviceIsUpwards = isUpright
    }

    private fun setDocumentType(type: String) {
        abbyyOCR.setDocumentType(type)
    }

    fun updateEulerAngles(headEulerAngleY: Float, headEulerAngleZ: Float) {
        identityCameraView?.updateEulerAngles(headEulerAngleY, headEulerAngleZ)
    }

}