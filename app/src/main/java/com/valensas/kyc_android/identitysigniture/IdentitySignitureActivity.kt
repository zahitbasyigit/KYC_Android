package com.valensas.kyc_android.identitysigniture

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.AmazonRekognitionClient
import com.amazonaws.services.rekognition.model.CompareFacesRequest
import com.amazonaws.services.rekognition.model.Image
import com.valensas.kyc_android.R
import com.valensas.kyc_android.identityresult.IdentityResultActivity
import kotlinx.android.synthetic.main.activity_identity_signiture.*
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference
import java.nio.ByteBuffer


class IdentitySignitureActivity : AppCompatActivity() {

    private var faceSelfieFile: String? = null
    private var faceScannedFile: String? = null
    private var signitureScannedFile: String? = null
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

        faceSelfieFile = intent?.getStringExtra("SelfieFace")
        faceScannedFile = intent?.getStringExtra("DocumentFace")
        faceSelfieByteArray = loadByteBufferFromFile(faceSelfieFile)
        faceScanByteArray = loadByteBufferFromFile(faceScannedFile)
        println(faceSelfieByteArray)
        println(faceScanByteArray)
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
            identitySignitureDrawView.allowInput = false
            identitySignitureRetryButton.setTextColor(Color.BLACK)
            identitySignitureContinueButton.setTextColor(Color.BLACK)
            spinnerView.visibility = View.VISIBLE

            FaceMatchAsyncTask(this@IdentitySignitureActivity).execute()
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

    private fun displayResultActivity(similarity: Float) {

        intent = Intent(this, IdentityResultActivity::class.java)
        intent.putExtra("FaceSimilarityPercentage", similarity)
        putImageToIntent("DrawnSigniture", intent, identitySignitureDrawView.bitmap)
        intent.putExtra("SelfieFace", faceSelfieFile)
        intent.putExtra("DocumentFace", faceScannedFile)
        intent.putExtra("Name", name)
        intent.putExtra("Surname", surname)
        intent.putExtra("TCKN", tckn)
        intent.putExtra("Birthday", birthday)
        startActivity(intent)
    }

    private fun putImageToIntent(name: String, intent: Intent, bitmap: Bitmap?) {
        if (bitmap != null) {
            val bs = ByteArrayOutputStream()
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 360, 360, false)
            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, bs)
            intent.putExtra(name, bs.toByteArray())
        }
    }

    private fun loadByteBufferFromFile(file: String?): ByteBuffer {
        return loadByteBufferfromBitmap(file?.let { loadImageFromFile(it) })
    }

    private fun loadImageFromFile(file: String): Bitmap? {
        return BitmapFactory.decodeStream(openFileInput(file))
    }

    private fun loadByteBufferfromBitmap(bitmap: Bitmap?): ByteBuffer {
        if (bitmap != null) {
            val bytes = bitmap.byteCount
            val buffer = ByteBuffer.allocate(bytes)
            bitmap.copyPixelsToBuffer(buffer)
            return buffer
//            val byteArray = buffer.array()
            //           return ByteBuffer.wrap(byteArray)
        }
        return ByteBuffer.allocate(0)
    }


    class FaceMatchAsyncTask(activity: IdentitySignitureActivity) : AsyncTask<String, Void, Boolean>() {
        private var activityReference: WeakReference<IdentitySignitureActivity> = WeakReference(activity)
        var faceSimilarity: Float = 0F

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg urls: String?): Boolean {
            val credentialsProvider = CognitoCachingCredentialsProvider(
                    activityReference.get()?.applicationContext, /* get the context for the application */
                    "us-east-1:facf575d-3636-4236-a54e-77f81d10b3d5", /* Identity Pool ID */
                    Regions.US_EAST_1           /* Region for your identity pool--US_EAST_1 or EU_WEST_1*/
            )

            val client = AmazonRekognitionClient(credentialsProvider)


            val sourceBuffer = activityReference.get()?.faceSelfieByteArray
            val targetBuffer = activityReference.get()?.faceScanByteArray

            println(sourceBuffer)
            println(targetBuffer)

            val source = Image()
                    .withBytes(sourceBuffer)
            val target = Image()
                    .withBytes(targetBuffer)

            val request = CompareFacesRequest()
                    .withSourceImage(source)
                    .withTargetImage(target)
                    .withSimilarityThreshold(0F)

            // Call operation
            val compareFacesResult = client.compareFaces(request)


            // Display results
            val faceDetails = compareFacesResult.getFaceMatches()
            val match = faceDetails[0]
            val face = match.getFace()
            faceSimilarity = face.confidence

            return true
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            activityReference.get()?.displayResultActivity(faceSimilarity)
        }
    }

}