package com.valensas.kyc_android.identityviacamera

import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.valensas.kyc_android.R
import kotlinx.android.synthetic.main.activity_identity_camera.*

class IdentityCameraActivity : AppCompatActivity(), IdentityCameraView {

    private var camera: Camera? = null
    private var cameraPreview: IdentityCameraPreview? = null
    private var identityCameraPresenter: IdentityCameraPresenter? = null
    private var flowState = state.STATE_FRONT_START

    enum class state {
        STATE_FRONT_START,
        STATE_FRONT_SCAN,
        STATE_BACK_START,
        STATE_BACK_SCAN,
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
        try {
            camera = Camera.open()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (camera != null) {
            cameraPreview = IdentityCameraPreview(this, camera!!)
            cameraview.addView(cameraPreview, 0)
            //cameraview.addView(cameraPreview)
        }
    }

    private fun initButtonListeners() {
        identityCameraInfoOKButton.setOnClickListener({
            identityCameraInfoFront.visibility = View.VISIBLE
            identityCameraInfoBack.visibility = View.VISIBLE
            identityCameraInfoImage.visibility = View.GONE
            identityCameraInfoText.visibility = View.GONE
            identityCameraInfoOKButton.visibility = View.GONE
            identityCameraPresenter?.listenFrontIdentityScan()
            flowState = state.STATE_FRONT_SCAN
        }

        )
    }

    override fun frontScanCompleted() {
        flowState = state.STATE_BACK_START
        identityCameraInfoImage.setImageResource(R.drawable.kyc_identity_back)
        identityCameraInfoImage.visibility = View.VISIBLE

        identityCameraInfoText.text = getString(R.string.identityCameraInfoBackText)
        identityCameraInfoText.visibility = View.VISIBLE

        identityCameraInfoOKButton.setOnClickListener({
            identityCameraInfoFront.visibility = View.VISIBLE
            identityCameraInfoBack.visibility = View.VISIBLE
            identityCameraInfoImage.visibility = View.GONE
            identityCameraInfoText.visibility = View.GONE
            identityCameraInfoOKButton.visibility = View.GONE
            identityCameraPresenter?.listenBackIdentityScan()
            flowState = state.STATE_BACK_SCAN
        })
        identityCameraInfoOKButton.visibility = View.VISIBLE
        identityCameraInfoFront.setImageResource(R.drawable.kyc_icon_identity_checked)
    }

    override fun backScanCompleted() {
        //TODO
    }


    override fun onBackPressed() {
        if (flowState == state.STATE_FRONT_SCAN)
            frontScanCompleted()
        else if (flowState == state.STATE_BACK_SCAN)
            backScanCompleted()
    }


}
