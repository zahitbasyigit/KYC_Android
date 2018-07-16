package com.valensas.kyc_android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.budiyev.android.codescanner.*
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector

class MainActivity : AppCompatActivity(), MainView {
    private var mainPresenter: MainPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainPresenter = MainPresenter()
        mainPresenter?.attach(this)

    }

    override fun onDestroy() {
        super.onDestroy()
        mainPresenter?.detach()
    }
}
