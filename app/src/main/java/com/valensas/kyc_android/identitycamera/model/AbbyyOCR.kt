package com.valensas.kyc_android.identitycamera.model

import android.graphics.Rect
import android.os.AsyncTask
import android.util.Log
import com.abbyy.mobile.rtr.Engine
import com.abbyy.mobile.rtr.IRecognitionService
import com.abbyy.mobile.rtr.ITextCaptureService
import com.abbyy.mobile.rtr.Language
import com.valensas.kyc_android.identitycamera.IdentityCameraPresenter
import com.valensas.kyc_android.identitycamera.model.document.DriversLicence
import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Created by Zahit on 20-Jul-18.
 */

class AbbyyOCR(val identityCameraPresenter: IdentityCameraPresenter) {
    private val licenseFileName = "licences/AbbyyRtrSdk.license"

    private var engine: Engine? = null
    private var textCaptureService: ITextCaptureService? = null

    private var currentBuffer: ByteArray? = null
    private var changeBuffer = AtomicBoolean(true)

    private var driversLicence = DriversLicence()

    private val textCaptureCallback = object : ITextCaptureService.Callback {
        override fun onRequestLatestFrame(buffer: ByteArray?) {
            //textCaptureService?.submitRequestedFrame(buffer)
            changeBuffer.set(false)
            fillBuffer(buffer)
            changeBuffer.set(true)
        }

        override fun onFrameProcessed(
                lines: Array<out ITextCaptureService.TextLine>?,
                status: IRecognitionService.ResultStabilityStatus?,
                warning: IRecognitionService.Warning?) {

            if (status?.ordinal!! < 4)
                return

            var terminate = false

            if (lines != null && lines.isNotEmpty()) {
                for (line in lines) {
                    for (category in driversLicence.document.documentItems.keys) {
                        val item = driversLicence.document.documentItems[category]

                        if (!driversLicence.document.skipWords.contains(line.Text))
                            item?.attemptToWrite(line.Text)
                    }
                    println(line.Text)
                    terminate = driversLicence.document.shouldTerminate()
                }
            }

            driversLicence.document.print()

            if (terminate) {
                identityCameraPresenter.frontTextScanSuccessful(driversLicence)
            }


        }

        override fun onError(e: Exception?) {
            e?.printStackTrace()
        }

    }

    fun createTextCaptureService(): Boolean {
        try {
            engine = Engine.load(identityCameraPresenter.getContext(), licenseFileName)
            textCaptureService = engine?.createTextCaptureService(textCaptureCallback)
            textCaptureService?.setRecognitionLanguage(Language.Turkish)
            return true
        } catch (e: java.io.IOException) {
            Log.e("Text Capture", "Error loading resource files")
            e.printStackTrace()
        } catch (e: Engine.LicenseException) {
            Log.e("Text Capture", "Licence file error")
            e.printStackTrace()
        } catch (e: Throwable) {
            e.printStackTrace()
            Log.e("Text Capture", "unspecified error")
            e.printStackTrace()
        }
        return false
    }

    fun startRecognition(width: Int, height: Int, rotation: Int) {
        val rectangle = Rect(0, 0, width, height)
        textCaptureService?.start(width, height, rotation - 90, rectangle)
    }

    fun stopRecognition() {
        class task : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                textCaptureService?.stop()
                return null
            }

            override fun onPostExecute(result: Void?) {
            }
        }
        task().execute()
    }

    fun receiveBuffer(buffer: ByteArray?) {
        if (changeBuffer.get())
            currentBuffer = buffer
    }

    fun fillBuffer(buffer: ByteArray?) {
        copyContents(currentBuffer, buffer)
        textCaptureService?.submitRequestedFrame(buffer)
    }

    fun copyContents(from: ByteArray?, to: ByteArray?) {
        if (from != null && to != null) {
            if (from.size > to.size) {
                println("From is greater than to! Cannot copy! From: ${from.size},To: ${to.size}")
            } else {
                for (i in from.indices) {
                    to[i] = from[i]
                }
            }
        }
    }
}