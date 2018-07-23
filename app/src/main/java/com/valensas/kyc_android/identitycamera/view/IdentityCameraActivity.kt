package com.valensas.kyc_android.identitycamera.view

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.DragAndDropPermissions
import android.view.View
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.Facing
import com.valensas.kyc_android.R
import com.valensas.kyc_android.identitycamera.IdentityCameraPresenter
import com.valensas.kyc_android.identitycamera.model.MySize
import com.valensas.kyc_android.identitycamera.model.document.Document
import com.valensas.kyc_android.identitycamera.model.driverslicence.DriversLicence
import com.valensas.kyc_android.identitysigniture.IdentitySignitureActivity
import kotlinx.android.synthetic.main.activity_identity_camera.*
import java.io.ByteArrayOutputStream
import java.security.AccessController.getContext

class IdentityCameraActivity : AppCompatActivity(), IdentityCameraView {

    private var identityCameraPresenter: IdentityCameraPresenter? = null
    private var driversLicence: DriversLicence? = null

    enum class state {
        STATE_FRONT_START,
        STATE_FRONT_SCAN,
        STATE_BACK_START,
        STATE_BACK_SCAN,
        STATE_SELFIE_START,
        STATE_SELFIE_SCAN,
        STATE_COMPLETE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_camera)
        identityCameraPresenter = IdentityCameraPresenter()
        identityCameraPresenter?.attach(this)
        initCamera()
        initButtonListeners()
    }

    private fun initCamera() {

    }

    private fun initButtonListeners() {
        identityCameraInfoOKButton.setOnClickListener({
            identityCameraInfoFront.visibility = View.VISIBLE
            identityCameraInfoBack.visibility = View.VISIBLE
            identityCameraInfoImage.visibility = View.GONE
            identityCameraInfoText.visibility = View.GONE
            identityCameraInfoOKButton.visibility = View.GONE

            val rect = Rect(0, 0, 0, 0)
            cameraView?.getWindowVisibleDisplayFrame(rect)
            println(rect.width())
            println(rect.height())

            identityCameraPresenter?.listenFrontIdentityScan()
            flowState = state.STATE_FRONT_SCAN

        }

        )
    }

    override fun frontScanCompleted(driversLicence: DriversLicence) {
        runOnUiThread({
            this.driversLicence = driversLicence
            driversLicence.finalizeDocument()
            flowState = state.STATE_BACK_START
            identityCameraInfoFront.setImageResource(R.drawable.kyc_icon_identity_checked)
            identityCameraInfoImage.setImageResource(R.drawable.kyc_identity_back)
            identityCameraInfoText.text = getString(R.string.identityCameraInfoBackText)

            identityCameraInfoImage.visibility = View.VISIBLE
            identityCameraInfoText.visibility = View.VISIBLE
            identityCameraInfoOKButton.visibility = View.VISIBLE

            identityCameraInfoOKButton.setOnClickListener({
                identityCameraInfoImage.visibility = View.GONE
                identityCameraInfoText.visibility = View.GONE
                identityCameraInfoOKButton.visibility = View.GONE
                identityCameraPresenter?.listenBackIdentityScan()
                flowState = state.STATE_BACK_SCAN
            })
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
        identityCameraInfoOKButton.visibility = View.VISIBLE


        identityCameraInfoOKButton.setOnClickListener({
            identityCameraInfoImage.visibility = View.GONE
            identityCameraInfoText.visibility = View.GONE
            identityCameraInfoOKButton.visibility = View.GONE
            identityCameraPresenter?.listenSelfieScan()
            flowState = state.STATE_SELFIE_SCAN
        })

        cameraView.facing = Facing.FRONT
    }

    override fun selfieScanCompleted(faceBitmap: Bitmap) {
        flowState = state.STATE_COMPLETE
        identityCameraInfoSelfie.setImageResource(R.drawable.kyc_icon_face_checked)

        //Signature Intent
        intent = Intent(this, IdentitySignitureActivity::class.java)
        putImageToIntent("SelfieFace", intent, faceBitmap)
        intent.putExtra("TCKN", driversLicence?.tckn)
        intent.putExtra("Name", driversLicence?.name)
        intent.putExtra("Surname", driversLicence?.surname)
        intent.putExtra("Birthday", driversLicence?.birthdate)

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

    }


}