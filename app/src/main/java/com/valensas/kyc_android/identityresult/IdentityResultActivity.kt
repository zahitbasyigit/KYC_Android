package com.valensas.kyc_android.identityresult

import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.valensas.kyc_android.R
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_identity_result.*

class IdentityResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_result)
        loadImageFromBundleToView("DrawnSigniture", identityResultSignitureImage)
        loadImageFromBundleToView("SelfieFace", identityResultSelfieImage)
    }


    private fun loadImageFromBundleToView(name: String, imageView: ImageView) {
        if (getIntent().hasExtra(name)) {
            val bitmap = BitmapFactory.decodeByteArray(getIntent().getByteArrayExtra(name), 0, getIntent().getByteArrayExtra(name).size)
            imageView.setImageBitmap(bitmap)
        }
    }
}
