package com.valensas.kyc_android.identityviacamera

import com.otaliastudios.cameraview.CameraView
import com.valensas.kyc_android.BaseView

/**
 * Created by Zahit on 17-Jul-18.
 */
interface IdentityCameraView : BaseView {
    fun frontScanCompleted()
    fun backScanCompleted()
    fun selfieScanCompleted()
    fun getCameraView(): CameraView

}