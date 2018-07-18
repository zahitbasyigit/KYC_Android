package com.valensas.kyc_android.facedetection

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.FirebaseApp
import com.valensas.kyc_android.R
import com.valensas.kyc_android.R.id.cameraView
import kotlinx.android.synthetic.main.activity_face_detection.*

class FaceDetectionActivity : AppCompatActivity(), FaceDetectionView {
    private var faceDetectionPresenter: FaceDetectionPresenter? = null

    private val faceDetector: FaceDetector by lazy {
        FaceDetector(faceDetectionPresenter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_detection)


        FirebaseApp.initializeApp(this)
        faceDetectionPresenter = FaceDetectionPresenter()
        faceDetectionPresenter?.attach(this)
        initCamera()
    }

    private fun initCamera() {
        /*cameraView.addFrameProcessor {
            faceDetector.process(Frame(
                    data = it.data,
                    rotation = it.rotation,
                    size = Size(it.size.width, it.size.height),
                    format = it.format,
                    isCameraFacingBack = cameraView.facing == Facing.BACK))
        }
*/
    }

    override fun faceDetectionSuccessful(successful: Boolean) {
        faceSuccessfulTextView.text = if (successful) "successful" else "unsuccessful"
    }


    override fun onResume() {
        super.onResume()
        //cameraView.start()
    }

    override fun onPause() {
        super.onPause()
        //cameraView.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        //cameraView.destroy()
    }
}
