package com.valensas.kyc_android.identitycamera.view

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import com.otaliastudios.cameraview.CameraView
import com.valensas.kyc_android.base.BaseView
import com.valensas.kyc_android.identitycamera.model.document.DocumentItemSet

/**
 * Created by Zahit on 17-Jul-18.
 */
interface IdentityCameraView : BaseView {
    fun frontScanCompleted(documentItemSet: DocumentItemSet, faceBitmap: Bitmap)
    fun backScanCompleted()
    fun selfieScanCompleted(faceBitmap: Bitmap)
    fun selfieBlinkScanCompleted(faceBitmap: Bitmap)
    fun setSpeechRecognitionText(required: String)
    fun speechRecognitionCompleted(message: String)
    fun speechRecognitionFailed(message: String)
    fun getCameraView(): CameraView
    fun getDefaultRotation(): Int
    fun getActivityContext(): Context
    fun getActivityAssets(): AssetManager
    fun updateEulerAngles(y: Float, z: Float)

}