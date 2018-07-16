package com.valensas.kyc_android

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * Created by Zahit on 16-Jul-18.
 */
class WelcomeView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    init {
        inflate(context, R.layout.welcome_1, null)
    }
}