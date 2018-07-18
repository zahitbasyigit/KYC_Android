package com.valensas.kyc_android.identityviacamera

import com.valensas.kyc_android.BasePresenter

/**
 * Created by Zahit on 17-Jul-18.
 */
class IdentityCameraPresenter : BasePresenter<IdentityCameraView> {
    private var identityCameraView: IdentityCameraView? = null

    override fun attach(view: IdentityCameraView) {
        identityCameraView = view

    }

    override fun detach() {
        identityCameraView = null
    }

    fun listenFrontIdentityScan() {

    }

    fun listenBackIdentityScan() {

    }

    fun listenSelfieScan() {

    }

    fun frontScanCompleted() {
        identityCameraView?.frontScanCompleted()
    }

    fun backScanCompleted() {
        identityCameraView?.backScanCompleted()
    }

    fun selfieScanCompleted() {
        identityCameraView?.selfieScanCompleted()
    }

}