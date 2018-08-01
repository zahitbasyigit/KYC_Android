package com.valensas.kyc_android.identitysigniture

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.valensas.kyc_android.R
import com.valensas.kyc_android.identityresult.IdentityResultActivity
import kotlinx.android.synthetic.main.activity_identity_signiture.*
import java.io.ByteArrayOutputStream
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.AmazonRekognition
import com.amazonaws.services.rekognition.AmazonRekognitionClient
import com.amazonaws.services.rekognition.model.Image
import com.amazonaws.services.rekognition.model.BoundingBox
import com.amazonaws.services.rekognition.model.CompareFacesMatch
import com.amazonaws.services.rekognition.model.CompareFacesRequest
import com.amazonaws.services.rekognition.model.CompareFacesResult
import com.amazonaws.services.rekognition.model.ComparedFace
import java.nio.ByteBuffer


class IdentitySignitureActivity : AppCompatActivity() {

    private var faceSelfieBitmap: Bitmap? = null
    private var faceScannedBitmap: Bitmap? = null
    private var signitureScannedBitmap: Bitmap? = null
    private var tckn: String? = null
    private var name: String? = null
    private var surname: String? = null
    private var birthday: String? = null
    private lateinit var faceSelfieByteArray: ByteBuffer
    private lateinit var faceScanByteArray: ByteBuffer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_signiture)
        AWSMobileClient.getInstance().initialize(this) {
            Log.d("aws", "AWSMobileClient is initialized")
        }.execute()

        faceSelfieBitmap = loadImageFromBundle("SelfieFace")
        faceScannedBitmap = loadImageFromBundle("DocumentFace")
        faceSelfieByteArray = loadByteArrayfromBundle("SelfieFace")
        faceScanByteArray = loadByteArrayfromBundle("DocumentFace")
        tckn = intent?.getStringExtra("TCKN")
        name = intent?.getStringExtra("Name")
        surname = intent?.getStringExtra("Surname")
        birthday = intent?.getStringExtra("Birthday")

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

            val credentialsProvider = CognitoCachingCredentialsProvider(
                    applicationContext, /* get the context for the application */
                    "COGNITO_IDENTITY_POOL", /* Identity Pool ID */
                    Regions.US_EAST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
            )

            val client = AmazonRekognitionClient(credentialsProvider)


            val source = Image()
                    .withBytes(faceSelfieByteArray)
            val target = Image()
                    .withBytes(faceScanByteArray)

            val request = CompareFacesRequest()
                    .withSourceImage(source)
                    .withTargetImage(target)
                    .withSimilarityThreshold(60F)

            // Call operation
            val compareFacesResult = client.compareFaces(request)


            // Display results
            val faceDetails = compareFacesResult.getFaceMatches()
            val match =faceDetails[0]
            val face = match.getFace()
            val facesimilarity = face.confidence




            intent = Intent(this, IdentityResultActivity::class.java)
            intent.putExtra("FaceSimilarityPercentage", facesimilarity)

            putImageToIntent("DrawnSigniture", intent, identitySignitureDrawView.bitmap)
            putImageToIntent("SelfieFace", intent, faceSelfieBitmap)
            putImageToIntent("DocumentFace", intent, faceScannedBitmap)
            intent.putExtra("Name", name)
            intent.putExtra("Surname", surname)
            intent.putExtra("TCKN", tckn)
            intent.putExtra("Birthday", birthday)
            startActivity(intent)
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

    private fun putImageToIntent(name: String, intent: Intent, bitmap: Bitmap?) {
        if (bitmap != null) {
            val bs = ByteArrayOutputStream()
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false)
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 50, bs)
            intent.putExtra(name, bs.toByteArray())
        }
    }

    private fun loadImageFromBundle(name: String): Bitmap? {
        if (getIntent().hasExtra(name)) {
            return BitmapFactory.decodeByteArray(getIntent().getByteArrayExtra(name), 0, getIntent().getByteArrayExtra(name).size)
        }
        return null
    }

    private fun loadByteArrayfromBundle(name:String): ByteBuffer {

            return ByteBuffer.wrap(getIntent().getByteArrayExtra(name))

    }
}
