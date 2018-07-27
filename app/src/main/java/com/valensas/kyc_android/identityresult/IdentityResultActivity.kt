package com.valensas.kyc_android.identityresult

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.valensas.kyc_android.R
import com.valensas.kyc_android.identitycamera.view.IdentityCameraActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_identity_result.*

class IdentityResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_result)
        loadImageFromBundleToView("DrawnSigniture", identityResultSignitureImage)
        loadImageFromBundleToView("SelfieFace", identityResultSelfieImage)
        loadImageFromBundleToView("DocumentFace", identityResultIDImage)
        loadStringFromBundleToTextView("Name", identityResultName)
        loadStringFromBundleToTextView("Surname", identityResultSurname)
        loadStringFromBundleToTextView("TCKN", identityResultTCKN)
        loadStringFromBundleToTextView("Birthday", identityResultBirthdate)
        initButtonListeners()
    }


    private fun initButtonListeners() {
        identityResultBackButton.setOnClickListener {
            createConfirmationDialog()
        }

        identityResultOKButton.setOnClickListener {

        }

    }

    private fun createConfirmationDialog() {
        val dialog = AlertDialog.Builder(this)
                .setMessage(R.string.identityResultBackWarningMessage)
                .setTitle(R.string.identityResultBackWarningTitle)
                .setPositiveButton(R.string.identityResultBackWarningPositiveButton, { dialog, _ -> resetBackToCameraIdentity() })
                .setNegativeButton(R.string.identityResultBackWarningNegativeButton, { dialog, _ -> dialog.dismiss() })
                .create() as AlertDialog
        dialog.show()
    }

    private fun resetBackToCameraIdentity() {
        intent = Intent(this, IdentityCameraActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun loadImageFromBundleToView(name: String, imageView: CircleImageView) {
        if (getIntent().hasExtra(name)) {
            val bitmap = BitmapFactory.decodeByteArray(intent.getByteArrayExtra(name), 0, intent.getByteArrayExtra(name).size)
            imageView.setImageBitmap(bitmap)
        }
    }

    private fun loadStringFromBundleToTextView(name: String, textView: TextView) {
        textView.text = getIntent().getStringExtra(name)
    }
}
