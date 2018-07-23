package com.valensas.kyc_android.identitycamera.view

import android.content.Context
import android.graphics.Bitmap
import com.otaliastudios.cameraview.CameraView
import com.valensas.kyc_android.base.BaseView
import com.valensas.kyc_android.identitycamera.model.document.Document
import com.valensas.kyc_android.identitycamera.model.driverslicence.DriversLicence

/**
 * Created by Zahit on 17-Jul-18.
 */
interface IdentityCameraView : BaseView {
    fun frontScanCompleted(driversLicence: DriversLicence)
    fun backScanCompleted()
    fun selfieScanCompleted(faceBitmap: Bitmap)
    fun getCameraView(): CameraView
    fun getDefaultRotation(): Int
    fun getActivityContext(): Context

}