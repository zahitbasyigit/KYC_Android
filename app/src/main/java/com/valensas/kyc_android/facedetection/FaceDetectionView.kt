package com.valensas.kyc_android.facedetection

import com.valensas.kyc_android.MainView

/**
 * Created by Zahit on 16-Jul-18.
 */
interface FaceDetectionView : MainView {
    fun faceDetectionSuccessful(successful: Boolean)

}