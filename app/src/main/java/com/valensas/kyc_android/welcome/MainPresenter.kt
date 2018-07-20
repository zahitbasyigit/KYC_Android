package com.valensas.kyc_android.welcome

import com.valensas.kyc_android.base.BasePresenter

/**
 * Created by Zahit on 16-Jul-18.
 */
class MainPresenter() : BasePresenter<MainView> {
    var mainView: MainView? = null

    override fun attach(view: MainView) {
        mainView = view
    }

    override fun detach() {
        mainView = null
    }

}