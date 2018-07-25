package com.valensas.kyc_android

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_image_test.*

class ImageTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_test)
        testImageView.setImageBitmap(imageBitmap)
    }

    companion object {
        var imageBitmap: Bitmap? = null

    }
}
