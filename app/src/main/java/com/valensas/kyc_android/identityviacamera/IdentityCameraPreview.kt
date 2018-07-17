package com.valensas.kyc_android.identityviacamera

import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException


/**
 * Created by Zahit on 17-Jul-18.
 */
class IdentityCameraPreview(context: Context, private val mCamera: Camera) : SurfaceView(context), SurfaceHolder.Callback {
    private val mHolder: SurfaceHolder

    init {
        mCamera.setDisplayOrientation(90)
        mHolder = holder
        mHolder.addCallback(this)
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            mCamera.setPreviewDisplay(holder)
            mCamera.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {

        if (mHolder.surface == null) {
            return
        }

        try {
            mCamera.stopPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mCamera.setPreviewDisplay(mHolder)
            mCamera.startPreview()

        } catch (e: Exception) {
        }

    }
}