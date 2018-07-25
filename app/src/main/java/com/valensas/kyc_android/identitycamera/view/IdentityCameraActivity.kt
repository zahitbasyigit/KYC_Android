package com.valensas.kyc_android.identitycamera.view

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.Facing
import com.valensas.kyc_android.ImageTestActivity
import com.valensas.kyc_android.R
import com.valensas.kyc_android.identitycamera.IdentityCameraPresenter
import com.valensas.kyc_android.identitycamera.model.document.DocumentItemSet
import com.valensas.kyc_android.identitycamera.model.document.DriversLicence
import com.valensas.kyc_android.identitycamera.model.tensorflow.TensorFlowImageClassifier
import com.valensas.kyc_android.identitysigniture.IdentitySignitureActivity
import kotlinx.android.synthetic.main.activity_identity_camera.*
import org.tensorflow.TensorFlow
import java.io.ByteArrayOutputStream

class IdentityCameraActivity : AppCompatActivity(), IdentityCameraView {
    override fun showBitmap(frameBitmap: Bitmap?) {
        flowState = state.STATE_COMPLETE
        runOnUiThread({
            ImageTestActivity.imageBitmap = frameBitmap
            intent = Intent(this, ImageTestActivity::class.java)
            startActivity(intent)
        })
    }

    enum class state {
        STATE_FRONT_START,
        STATE_FRONT_SCAN,
        STATE_BACK_START,
        STATE_BACK_SCAN,
        STATE_SELFIE_START,
        STATE_SELFIE_SCAN,
        STATE_COMPLETE
    }

    private var identityCameraPresenter: IdentityCameraPresenter? = null
    private var documentItemSet: DocumentItemSet? = null
    private var documentFaceBitmap: Bitmap? = null
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
        initButtonListeners()
    }

    private fun initButtonListeners() {
        identityCameraInfoOKButton.visibility = View.GONE
        handler.sendEmptyMessageDelayed(INITIALIZE_FRONT_SCAN, INFO_READ_WAIT_TIME)
    }

    fun initializeFrontScan() {
        identityCameraInfoFront.visibility = View.VISIBLE
        identityCameraInfoBack.visibility = View.VISIBLE
        identityCameraInfoImage.visibility = View.GONE
        identityCameraInfoText.visibility = View.GONE
        identityCameraInfoOKButton.visibility = View.GONE
        identityCameraPresenter?.listenFrontIdentityScan()
        flowState = state.STATE_FRONT_SCAN
    }

    fun initializeBackScan() {
        identityCameraInfoImage.visibility = View.GONE
        identityCameraInfoText.visibility = View.GONE
        identityCameraInfoOKButton.visibility = View.GONE
        identityCameraPresenter?.listenBackIdentityScan()
        flowState = state.STATE_BACK_SCAN
    }

    fun initializeSelfieScan() {
        identityCameraInfoImage.visibility = View.GONE
        identityCameraInfoText.visibility = View.GONE
        identityCameraInfoOKButton.visibility = View.GONE
        identityCameraPresenter?.listenSelfieScan()
        flowState = state.STATE_SELFIE_SCAN
    }

    override fun frontScanCompleted(documentItemSet: DocumentItemSet, faceBitmap: Bitmap) {
        runOnUiThread({
            this.documentItemSet = documentItemSet
            this.documentFaceBitmap = faceBitmap
            documentItemSet.finalizeDocument()
            flowState = state.STATE_BACK_START
            identityCameraInfoFront.setImageResource(R.drawable.kyc_icon_identity_checked)
            identityCameraInfoImage.setImageResource(R.drawable.kyc_identity_back)
            identityCameraInfoText.text = getString(R.string.identityCameraInfoBackText)

            identityCameraInfoImage.visibility = View.VISIBLE
            identityCameraInfoText.visibility = View.VISIBLE
            //identityCameraInfoOKButton.visibility = View.VISIBLE

            handler.sendEmptyMessageDelayed(INITIALIZE_BACK_SCAN, INFO_READ_WAIT_TIME)
        })

    }

    override fun backScanCompleted() {
        flowState = state.STATE_SELFIE_START
        identityCameraInfoImage.setImageResource(R.drawable.kyc_facescan)
        identityCameraInfoText.text = getString(R.string.identityCameraInfoSelfieText)

        identityCameraInfoBack.visibility = View.GONE
        identityCameraInfoFront.visibility = View.GONE
        identityCameraInfoSelfie.visibility = View.VISIBLE

        identityCameraInfoImage.visibility = View.VISIBLE
        identityCameraInfoText.visibility = View.VISIBLE
        //identityCameraInfoOKButton.visibility = View.VISIBLE

        handler.sendEmptyMessageDelayed(INITIALIZE_SELFIE_SCAN, INFO_READ_WAIT_TIME)
        cameraView.facing = Facing.FRONT
    }

    override fun selfieScanCompleted(faceBitmap: Bitmap) {
        flowState = state.STATE_COMPLETE
        identityCameraInfoSelfie.setImageResource(R.drawable.kyc_icon_face_checked)

        //Signature Intent
        intent = Intent(this, IdentitySignitureActivity::class.java)
        putImageToIntent("SelfieFace", intent, faceBitmap)
        putImageToIntent("DocumentFace", intent, documentFaceBitmap)
        intent.putExtra("TCKN", documentItemSet?.tckn)
        intent.putExtra("Name", documentItemSet?.name)
        intent.putExtra("Surname", documentItemSet?.surname)
        intent.putExtra("Birthday", documentItemSet?.birthdate)

        println(faceBitmap)
        startActivity(intent)
    }

    override fun getCameraView(): CameraView {
        return cameraView
    }

    override fun getDefaultRotation(): Int {
        return windowManager.defaultDisplay.rotation
    }

    override fun getActivityContext(): Context {
        return this
    }

    override fun getActivityAssets(): AssetManager {
        return assets
    }


    override fun onResume() {
        super.onResume()
        cameraView?.start()
    }

    override fun onPause() {
        super.onPause()
        cameraView?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun putImageToIntent(name: String, intent: Intent, bitmap: Bitmap?) {
        if (bitmap != null) {
            val bs = ByteArrayOutputStream()
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false)
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 50, bs)
            intent.putExtra(name, bs.toByteArray())
        }
    }

    companion object {
        var flowState = state.STATE_FRONT_START
        var INITIALIZE_FRONT_SCAN = 0
        var INITIALIZE_BACK_SCAN = 1
        var INITIALIZE_SELFIE_SCAN = 2
        var INFO_READ_WAIT_TIME = 3000L
    }


}
