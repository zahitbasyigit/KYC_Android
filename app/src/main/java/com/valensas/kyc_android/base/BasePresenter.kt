package com.valensas.kyc_android.base

/**
 * Created by Zahit on 16-Jul-18.
 */
interface BasePresenter<V> {
    fun attach(view : V)
    fun detach()
}