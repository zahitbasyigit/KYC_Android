package com.valensas.kyc_android.facedetection

import com.valensas.kyc_android.BasePresenter
import com.valensas.kyc_android.MainView

/**
 * Created by Zahit on 16-Jul-18.
 */
class FaceDetectionPresenter : BasePresenter<FaceDetectionView> {
    var mainView: FaceDetectionView? = null

    override fun attach(view: FaceDetectionView) {
        mainView = view
    }

    override fun detach() {
        mainView = null
    }

    fun faceDetectionSuccessful(successful: Boolean) {
        mainView?.faceDetectionSuccessful(successful)
    }

}