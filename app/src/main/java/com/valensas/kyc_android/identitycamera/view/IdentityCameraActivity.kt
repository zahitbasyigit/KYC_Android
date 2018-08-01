package com.valensas.kyc_android.identitycamera.view

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Camera
import android.graphics.PointF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.otaliastudios.cameraview.*
import com.valensas.kyc_android.R
import com.valensas.kyc_android.identitycamera.IdentityCameraPresenter
import com.valensas.kyc_android.identitycamera.model.document.DocumentItemSet
import com.valensas.kyc_android.identitycamera.view.IdentityCameraActivity.State.*
import com.valensas.kyc_android.identitysigniture.IdentitySignitureActivity
import kotlinx.android.synthetic.main.activity_identity_camera.*
import java.io.ByteArrayOutputStream

class IdentityCameraActivity : AppCompatActivity(), IdentityCameraView, SensorEventListener {

    private var identityCameraPresenter: IdentityCameraPresenter? = null
    private var documentItemSet: DocumentItemSet? = null
    private var documentFaceBitmap: Bitmap? = null
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor

    private var handler = Handler { msg ->
        when (msg.what) {
            INITIALIZE_FRONT_SCAN -> {
                initializeFrontScan()
                true
            }
            INITIALIZE_BACK_SCAN -> {
                initializeBackScan()
                true
            }

            INITIALIZE_SELFIE_SCAN -> {
                initializeSelfieScan()
                true
            }
            INITIALIZE_SPEECH_RECOGNITION -> {
                initializeSpeechRecognition()
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_camera)
        identityCameraPresenter = IdentityCameraPresenter()
        identityCameraPresenter?.attach(this)
        initializeSensors()
        initializeFlow()
        initializeCamera()
    }

    private fun initializeSensors() {
        //TODO initialize sensor during the right scan / remove as it finishes.
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    //edit this to change the starting scan
    private fun initializeFlow() {
        handler.sendEmptyMessageDelayed(INITIALIZE_FRONT_SCAN, INFO_READ_WAIT_TIME)
        flowState = FRONT_SCAN_PRE
    }

    private fun initializeCamera() {
        cameraView?.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER)
    }

    val focusListener = object : CameraListener() {
        override fun onFocusEnd(successful: Boolean, point: PointF?) {
            super.onFocusEnd(successful, point)
            if (successful)
                identityCameraPresenter?.listenFrontFaceScan()
        }
    }

    private fun initializeFrontScan() {
        identityCameraInfoFront.visibility = View.VISIBLE
        identityCameraInfoBack.visibility = View.VISIBLE
        identityCameraInfoImage.visibility = View.GONE
        identityCameraInfoText.visibility = View.GONE
        identityCameraPresenter?.listenFrontIdentityScan()
        flowState = FRONT_SCAN_DURING

        cameraView?.addCameraListener(focusListener)
    }

    private fun initializeBackScan() {
        identityCameraInfoImage.visibility = View.GONE
        identityCameraInfoText.visibility = View.GONE
        identityCameraPresenter?.listenBackIdentityScan()
        flowState = BACK_SCAN_DURING
    }

    private fun initializeSpeechRecognition() {
        identityCameraInfoImage.visibility = View.GONE
        identityCameraInfoText.visibility = View.GONE
        speechRecognitionText.visibility = View.VISIBLE
        identityCameraPresenter?.listenSpeechRecognition()
        flowState = SPEECH_RECOGNITION_DURING
    }

    private fun initializeSelfieScan() {
        identityCameraInfoImage.visibility = View.GONE
        identityCameraInfoText.visibility = View.GONE
        identityCameraWarningText.visibility = View.VISIBLE
        identityCameraPresenter?.listenSelfieScan()
        flowState = SELFIE_SCAN_DURING
    }


    override fun frontScanCompleted(documentItemSet: DocumentItemSet, faceBitmap: Bitmap) {
        runOnUiThread({
            this.documentItemSet = documentItemSet
            this.documentFaceBitmap = faceBitmap
            documentItemSet.finalizeDocument()
            flowState = BACK_SCAN_PRE
            identityCameraInfoFront.setImageResource(R.drawable.kyc_icon_identity_checked)
            identityCameraInfoImage.setImageResource(R.drawable.kyc_identity_back)
            identityCameraInfoText.text = getString(R.string.identityCameraInfoBackText)

            identityCameraInfoImage.visibility = View.VISIBLE
            identityCameraInfoText.visibility = View.VISIBLE

            cameraView?.removeCameraListener(focusListener)

            handler.sendEmptyMessageDelayed(INITIALIZE_BACK_SCAN, INFO_READ_WAIT_TIME)
        })
    }

    override fun backScanCompleted() {
        flowState = SPEECH_RECOGNITION_PRE
        identityCameraInfoImage.setImageResource(R.drawable.kyc_facescan)
        identityCameraInfoText.text = getString(R.string.identityCameraInfoSelfieText)

        identityCameraInfoBack.visibility = View.GONE
        identityCameraInfoFront.visibility = View.GONE

        speechRecognitionText.visibility = View.VISIBLE
        handler.sendEmptyMessageDelayed(INITIALIZE_SPEECH_RECOGNITION, INFO_READ_WAIT_TIME)

    }

    override fun speechRecognitionCompleted(result: String) {
        flowState = SELFIE_SCAN_PRE
        speechRecognitionText.visibility = View.INVISIBLE
        identityCameraInfoSelfie.visibility = View.VISIBLE

        identityCameraInfoImage.visibility = View.VISIBLE
        identityCameraInfoText.visibility = View.VISIBLE
        handler.sendEmptyMessageDelayed(INITIALIZE_SELFIE_SCAN, INFO_READ_WAIT_TIME)

        cameraView.facing = Facing.FRONT

    }

    override fun speechRecognitionFailed(result: String) {
        
    }

    override fun selfieScanCompleted(faceBitmap: Bitmap) {
        flowState = COMPLETE
        identityCameraInfoSelfie.setImageResource(R.drawable.kyc_icon_face_checked)

        //Signature Intent
        intent = Intent(this, IdentitySignitureActivity::class.java)
        putImageToIntent("SelfieFace", intent, faceBitmap)
        putImageToIntent("DocumentFace", intent, documentFaceBitmap)
        intent.putExtra("TCKN", documentItemSet?.tckn)
        intent.putExtra("Name", documentItemSet?.name)
        intent.putExtra("Surname", documentItemSet?.surname)
        intent.putExtra("Birthday", documentItemSet?.birthdate)

        startActivity(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER)
                gravity = event.values
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD)
                geomagnetic = event.values
            if (gravity != null && geomagnetic != null) {
                val r = FloatArray(9)
                val i = FloatArray(9)

                val success = SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)
                if (success) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)
                    updateDeviceOrientation(orientation[1])

                }
            }
        }
    }

    private fun updateDeviceOrientation(second: Float) {
        if (flowState == SELFIE_SCAN_DURING) {
            if (second < -1.4) {
                //identityCameraWarningText.text = "STRAIGHT"
            } else {
                // identityCameraWarningText.text = "NOT STRAIGHT"
            }
            identityCameraPresenter?.setDeviceIsUpright(second < -1.4)
        }
    }


    override fun getCameraView(): CameraView {
        return cameraView
    }

    override fun getDefaultRotation(): Int {
        return windowManager.defaultDisplay.rotation
    }

    override fun getActivityContext(): Context {
        return applicationContext
    }

    override fun getActivityAssets(): AssetManager {
        return assets
    }


    override fun onResume() {
        super.onResume()
        cameraView?.start()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        cameraView?.stop()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        identityCameraPresenter?.detach()
        sensorManager.unregisterListener(this)
    }

    private fun putImageToIntent(name: String, intent: Intent, bitmap: Bitmap?) {
        if (bitmap != null) {
            val bs = ByteArrayOutputStream()
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false)
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, bs)
            intent.putExtra(name, bs.toByteArray())
        }
    }

    override fun updateEulerAngles(y: Float, z: Float) {
        identityCameraWarningText.text = "Y angle : $y, Z angle : $z"
    }


    enum class State {
        SPEECH_RECOGNITION_PRE,
        SPEECH_RECOGNITION_DURING,
        FRONT_SCAN_PRE,
        FRONT_SCAN_DURING,
        BACK_SCAN_PRE,
        BACK_SCAN_DURING,
        SELFIE_SCAN_PRE,
        SELFIE_SCAN_DURING,
        COMPLETE
    }

    companion object {
        var flowState = FRONT_SCAN_PRE
        var INITIALIZE_FRONT_SCAN = 0
        var INITIALIZE_BACK_SCAN = 1
        var INITIALIZE_SELFIE_SCAN = 2
        var INITIALIZE_SPEECH_RECOGNITION = 3
        var INFO_READ_WAIT_TIME = 1500L
    }


}
