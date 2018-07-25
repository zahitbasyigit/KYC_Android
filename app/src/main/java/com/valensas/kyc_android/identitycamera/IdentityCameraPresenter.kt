package com.valensas.kyc_android.identitycamera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import com.otaliastudios.cameraview.Frame
import com.otaliastudios.cameraview.FrameProcessor
import com.valensas.kyc_android.R.id.view
import com.valensas.kyc_android.base.BasePresenter
import com.valensas.kyc_android.identitycamera.model.*
import com.valensas.kyc_android.identitycamera.model.document.DocumentItemSet
import com.valensas.kyc_android.identitycamera.model.document.DocumentItemSet.Type.*
import com.valensas.kyc_android.identitycamera.model.document.DriversLicence
import com.valensas.kyc_android.identitycamera.model.tensorflow.Classifier
import com.valensas.kyc_android.identitycamera.model.tensorflow.TensorFlowImageClassifier
import com.valensas.kyc_android.identitycamera.view.IdentityCameraView
import java.io.ByteArrayOutputStream

/**
 * Created by Zahit on 17-Jul-18.
 */
class IdentityCameraPresenter : BasePresenter<IdentityCameraView> {
    val MODEL_FILE = "file:///android_asset/retrained_graph.pb"
    val LABEL_FILE = "file:///android_asset/retrained_labels.txt"
    val INPUT_WIDTH = 224
    val INPUT_HEIGHT = 224
    val IMAGE_MEAN = 0
    val IMAGE_STD = 255f
    val INPUT_NAME = "input"
    val OUTPUT_NAME = "final_result"

    private var identityCameraView: IdentityCameraView? = null
    private var qrReader = FirebaseQRReader(this)
    private var faceDetector = FirebaseFaceDetection(this)
    private var abbyyOCR = AbbyyOCR(this)
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
            classifier = TensorFlowImageClassifier.create(
                    it, MODEL_FILE, LABEL_FILE, INPUT_WIDTH, INPUT_HEIGHT,
                    IMAGE_MEAN, IMAGE_STD, INPUT_NAME, OUTPUT_NAME)
        }

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

            this.frame = MyFrame(null, it.rotation, MySize(it.size.width, it.size.height), it.format, true)
            abbyyOCR.receiveBuffer(it.data)
        }

        frontFaceScanProcessor = FrameProcessor {
            faceDetector.process(it)
        }

        frontDocumentClassifier = FrameProcessor {
            val results = classifier.recognizeImage(convertFrameToBitmap(it))
            printResult(results)
        }



        identityCameraView?.getCameraView()?.addFrameProcessor(frontTextScanProcessor)
        identityCameraView?.getCameraView()?.addFrameProcessor(frontFaceScanProcessor)
        identityCameraView?.getCameraView()?.addFrameProcessor(frontDocumentClassifier)
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

    fun frontTextScanSuccessful(documentItemSet: DocumentItemSet) {
        this.frontTextScanCompleted = true
        this.documentItemSet = documentItemSet
        identityCameraView?.getCameraView()?.removeFrameProcessor(frontTextScanProcessor)
        abbyyOCR.stopRecognition()
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

    var frame: MyFrame? = null

    private fun convertFrameToBitmap(frame: Frame): Bitmap {
        val out = ByteArrayOutputStream()
        val yuvImage = YuvImage(frame.data, frame.format, frame.size.width, frame.size.height, null)
        yuvImage.compressToJpeg(Rect(0, 0, INPUT_WIDTH, INPUT_HEIGHT), 90, out)

        val imageBytes = out.toByteArray()
        val frameBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        out.flush()
        out.close()

        return frameBitmap
    }

    private fun printResult(result: List<Classifier.Recognition>?) {
        if (result != null && result.isEmpty())
            return

        println(result?.get(0)?.title)


    }

    fun showBuffer(bestBuffer: ByteArray?) {
        val myFrame = frame

        myFrame?.let {

            val out = ByteArrayOutputStream()
            val yuvImage = YuvImage(bestBuffer, myFrame.format, myFrame.size.width, myFrame.size.height, null)
            yuvImage.compressToJpeg(Rect(0, 0, myFrame.size.width, myFrame.size.height), 90, out)

            val imageBytes = out.toByteArray()
            val frameBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            out.flush()
            out.close()

            identityCameraView?.showBitmap(frameBitmap)
        }
    }
}