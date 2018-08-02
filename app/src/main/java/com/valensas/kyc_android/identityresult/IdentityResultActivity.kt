package com.valensas.kyc_android.identityresult

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.valensas.kyc_android.R
import com.valensas.kyc_android.identitycamera.view.IdentityCameraActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_identity_result.*
import java.nio.ByteBuffer


class IdentityResultActivity : AppCompatActivity() {

    private var facePercentageMatch: Float = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_result)

        loadInfo()
        initAnimations()
        initButtonListeners()
    }

    private fun loadInfo() {
        loadImageFromBundleToView("DrawnSigniture", identityResultSignitureImage)
        loadImageFromFileToView(intent.getStringExtra("SelfieFace"), identityResultSelfieImage)
        loadImageFromFileToView(intent.getStringExtra("DocumentFace"), identityResultIDImage)
        loadFloatFromBundleToTextView("FaceSimilarityPercentage", identityResultSelfiePercentageMatchingText)
        loadStringFromBundleToTextView("Name", identityResultName)
        loadStringFromBundleToTextView("Surname", identityResultSurname)
        loadStringFromBundleToTextView("TCKN", identityResultTCKN)
        loadStringFromBundleToTextView("Birthday", identityResultBirthdate)
    }

    private fun initAnimations() {
        var offset = 0L
        val imagesDuration = 350L
        val percentageImageDuration = 750L
        val percentageValueDuration = 2500L
        val expandCategoryDuration = 150L

        initScaleAnimation(identityResultIDImage, R.anim.scale_up_from_bottom_left, imagesDuration, offset)
        initScaleAnimation(identityResultSelfieImage, R.anim.scale_up_from_bottom_right, imagesDuration, offset)
        initScaleAnimation(identityResultSelfieMatchingText, R.anim.scale_up_from_center, imagesDuration, offset)

        offset += imagesDuration

        initScaleAnimation(identityResultSelfiePercentageMatchingText, R.anim.scale_up_from_center, percentageImageDuration, offset)
        addPercentageAnimator(identityResultSelfiePercentageMatchingText, 0, facePercentageMatch.toInt(), percentageValueDuration, offset)

        offset += percentageValueDuration

        initScaleAnimation(identityResultNameLine, R.anim.scale_up_from_left, expandCategoryDuration, offset)

        offset += expandCategoryDuration

        initScaleAnimation(identityResultNameTitle, R.anim.scale_up_from_top_left, expandCategoryDuration, offset)
        initScaleAnimation(identityResultSurnameLine, R.anim.scale_up_from_left, expandCategoryDuration, offset)

        offset += expandCategoryDuration

        initScaleAnimation(identityResultName, R.anim.scale_up_from_bottom_left, expandCategoryDuration, offset)
        initScaleAnimation(identityResultSurnameTitle, R.anim.scale_up_from_top_left, expandCategoryDuration, offset)
        initScaleAnimation(identityResultBirthdateLine, R.anim.scale_up_from_left, expandCategoryDuration, offset)

        offset += expandCategoryDuration

        initScaleAnimation(identityResultSurname, R.anim.scale_up_from_bottom_left, expandCategoryDuration, offset)
        initScaleAnimation(identityResultBirthdateTitle, R.anim.scale_up_from_top_left, expandCategoryDuration, offset)
        initScaleAnimation(identityResultTCKNLine, R.anim.scale_up_from_left, expandCategoryDuration, offset)

        offset += expandCategoryDuration

        initScaleAnimation(identityResultBirthdate, R.anim.scale_up_from_bottom_left, expandCategoryDuration, offset)
        initScaleAnimation(identityResultTCKNTitle, R.anim.scale_up_from_top_left, expandCategoryDuration, offset)

        offset += expandCategoryDuration

        initScaleAnimation(identityResultTCKN, R.anim.scale_up_from_bottom_left, expandCategoryDuration, offset)

        offset += expandCategoryDuration

        initScaleAnimation(identityResultIDSignitureImage, R.anim.scale_up_from_bottom_left, imagesDuration, offset)
        initScaleAnimation(identityResultSignitureImage, R.anim.scale_up_from_bottom_right, imagesDuration, offset)
        initScaleAnimation(identityResultSignitureMatchingText, R.anim.scale_up_from_center, imagesDuration, offset)

        offset += imagesDuration

        initScaleAnimation(identityResultSigniturePercentageMatchingText, R.anim.scale_up_from_center, percentageImageDuration, offset)
        addPercentageAnimator(identityResultSigniturePercentageMatchingText, 0, 80, percentageValueDuration, offset)
    }

    private fun initScaleAnimation(view: View, animID: Int, duration: Long, delay: Long) {
        val animation = AnimationUtils.loadAnimation(this, animID)
        animation.duration = duration
        animation.startOffset = delay
        view.startAnimation(animation)
    }

    private fun addPercentageAnimator(textView: TextView, initial: Int, final: Int, duration: Long, delay: Long) {
        val valueAnimator = ValueAnimator.ofInt(initial, final)
        valueAnimator.duration = duration
        valueAnimator.startDelay = delay
        valueAnimator.addUpdateListener {
            textView.text = "%" + valueAnimator.animatedValue.toString()
        }

        valueAnimator.start()
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
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun loadImageFromFileToView(file: String, imageView: CircleImageView): Bitmap? {
        return BitmapFactory.decodeStream(openFileInput(file))
    }

    private fun loadImageFromBundleToView(name: String, imageView: CircleImageView) {
        if (intent.hasExtra(name)) {
            val bitmap = BitmapFactory.decodeByteArray(intent.getByteArrayExtra(name), 0, intent.getByteArrayExtra(name).size)
            imageView.setImageBitmap(bitmap)
        }
    }

    private fun loadStringFromBundleToTextView(name: String, textView: TextView) {
        textView.text = intent.getStringExtra(name)
    }

    private fun loadFloatFromBundleToTextView(name: String, textView: TextView) {
        val floatvalue = getIntent().getFloatExtra(name, 0F)
        facePercentageMatch = floatvalue
        textView.text = Float.toString()
    }
}
