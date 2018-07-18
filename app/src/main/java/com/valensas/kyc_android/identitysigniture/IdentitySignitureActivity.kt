package com.valensas.kyc_android.identitysigniture

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.valensas.kyc_android.R
import kotlinx.android.synthetic.main.activity_identity_signiture.*


class IdentitySignitureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_signiture)
        initInfoDialog()
        initButtonListeners()
    }

    private fun initButtonListeners() {
        identitySignitureRetryButton.setOnClickListener({
            identitySignitureDrawView.clearDrawing()
        })

        identitySignitureContinueButton.setOnClickListener({
            identitySignitureRetryButton.setTextColor(Color.BLACK)
            identitySignitureContinueButton.setTextColor(Color.BLACK)
            spinnerView.visibility = View.VISIBLE
        })
    }

    private fun initInfoDialog() {
        val titleTextView = TextView(this)
        titleTextView.text = getString(R.string.identitySignitureAlertTitle)
        titleTextView.gravity = Gravity.CENTER
        titleTextView.textSize = 20F
        titleTextView.setTextColor(Color.BLACK)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, 20, 0, 0)
        titleTextView.setPadding(0, 30, 0, 0)
        titleTextView.setLayoutParams(params)

        val dialogBuilder = AlertDialog.Builder(this)
        val dialog = dialogBuilder
                .setMessage(R.string.identitySignitureAlertText)
                .setCustomTitle(titleTextView)
                .setPositiveButton(R.string.identitySignitureAlertPositiveButton, { dialog, _ -> dialog.dismiss() })
                .create() as AlertDialog
        dialog.show()

        val messageTextView = dialog.findViewById<TextView>(android.R.id.message)
        messageTextView.gravity = Gravity.CENTER

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setAllCaps(false)
        val positiveButtonLL = positiveButton.layoutParams as LinearLayout.LayoutParams
        positiveButtonLL.width = ViewGroup.LayoutParams.MATCH_PARENT
        positiveButton.layoutParams = positiveButtonLL

        dialog.window.setBackgroundDrawableResource(R.drawable.round_dialog_background)
    }
}
