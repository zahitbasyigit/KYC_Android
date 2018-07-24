package com.valensas.kyc_android.identitycamera.model

import android.graphics.*
import android.util.Log
import android.view.Surface
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.otaliastudios.cameraview.Frame
import com.valensas.kyc_android.identitycamera.IdentityCameraPresenter
import java.io.ByteArrayOutputStream


/**
 * Created by Zahit on 19-Jul-18.
 */
class FirebaseFaceDetection(val identityCameraPresenter: IdentityCameraPresenter?) {

    val firebaseFaceWrapper = FirebaseFaceWrapper()
    var detectionMode = DETECT_IN_DOCUMENT

    fun process(frame: Frame) {
        detectFaceIn(frame)
    }

    private fun detectFaceIn(frame: Frame) {
        frame.data?.let {
            val myFrame = MyFrame(frame.data.clone(), frame.rotation, MySize(frame.size.width, frame.size.height), frame.format, false)

            firebaseFaceWrapper.process(
                    image = convertFrameToImage(frame),
                    onSuccess = {
                        Log.d("Scanner", "Scanning Faces : ${this.detectionMode}")
                        if (it.isNotEmpty()) {
                            val face = it.first()
                            processImage(face, myFrame)
                        }
                    },
                    onError = {
                        //Nothing
                    })
        }
    }

    private fun convertFrameToImage(frame: Frame) =
            FirebaseVisionImage.fromByteArray(frame.data!!, extractFrameMetadata(frame))

    private fun extractFrameMetadata(frame: Frame): FirebaseVisionImageMetadata =
            FirebaseVisionImageMetadata.Builder()
                    .setWidth(frame.size.width)
                    .setHeight(frame.size.height)
                    .setFormat(frame.format)
                    .setRotation(frame.rotation /
                            when (this.detectionMode) {
                                DETECT_IN_DOCUMENT -> RIGHT_ANGLE * 2
                                DETECT_IN_SELFIE -> RIGHT_ANGLE
                                else -> {
                                    RIGHT_ANGLE
                                }
                            })
                    .build()


    private fun printBoundingBox(rect: Rect) {
        println("Rectangle : Left:${rect.left} , Right:${rect.right} , Top:${rect.top} , Bottom:${rect.bottom}")
    }

    private fun printImageDimensions(bitmap: Bitmap) {
        println("Image : Width:${bitmap.width} , Height:${bitmap.height}")
    }

    private fun printFrameDimensions(frame: MyFrame) {
        println("Frame : Width:${frame.size.width} , Height:${frame.size.height}")
    }

    private fun processImage(fbFace: FirebaseVisionFace, frame: MyFrame) {
        printBoundingBox(fbFace.boundingBox)
        printFrameDimensions(frame)

        if (fbFace.boundingBox.left > 0 && fbFace.boundingBox.right < frame.size.height &&
                fbFace.boundingBox.top > 0 && fbFace.boundingBox.bottom < frame.size.width) {
            val out = ByteArrayOutputStream()
            val yuvImage = YuvImage(frame.data, frame.format, frame.size.width, frame.size.height, null)
            yuvImage.compressToJpeg(Rect(0, 0, frame.size.width, frame.size.height), 90, out)

            val imageBytes = out.toByteArray()
            val frameBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            out.flush()
            out.close()

            val rotatedImage = rotateImage(frameBitmap)

            printImageDimensions(rotatedImage)
            printBoundingBox(fbFace.boundingBox)

            val croppedBitmap = Bitmap.createBitmap(
                    rotatedImage,
                    fbFace.boundingBox.left,
                    fbFace.boundingBox.top,
                    fbFace.boundingBox.width(),
                    fbFace.boundingBox.height()
            )


            when (detectionMode) {
                DETECT_IN_DOCUMENT -> {
                    identityCameraPresenter?.frontFaceScanSuccessful(croppedBitmap)
                }
                DETECT_IN_SELFIE -> {
                    identityCameraPresenter?.selfieFaceScanSuccessful(croppedBitmap)
                }
            }

        }
    }

    private fun rotateImage(img: Bitmap): Bitmap {
        Log.d("Orientation ", "${identityCameraPresenter?.getDefaultRotation()}")
        val matrix = Matrix()
        var rotationDegree = when (identityCameraPresenter?.getDefaultRotation()) {
            Surface.ROTATION_0 -> -90F
            Surface.ROTATION_90 -> 0F
            Surface.ROTATION_180 -> 90F
            Surface.ROTATION_270 -> 180F
            else -> 0F
        }

        if (detectionMode == DETECT_IN_DOCUMENT) {
            rotationDegree += 90F
        }

        matrix.postRotate(rotationDegree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        if (rotatedImg != img)
            img.recycle()
        return rotatedImg
    }

    companion object {
        private const val RIGHT_ANGLE = 90
        val DETECT_IN_DOCUMENT = 0
        val DETECT_IN_SELFIE = 1
    }

}