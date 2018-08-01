package com.valensas.kyc_android.identitycamera.model

import android.graphics.Rect
import android.os.AsyncTask
import android.util.Log
import com.abbyy.mobile.rtr.Engine
import com.abbyy.mobile.rtr.IRecognitionService
import com.abbyy.mobile.rtr.ITextCaptureService
import com.abbyy.mobile.rtr.Language
import com.valensas.kyc_android.identitycamera.IdentityCameraPresenter
import com.valensas.kyc_android.identitycamera.model.document.DocumentItemSet
import com.valensas.kyc_android.identitycamera.model.document.DriversLicence
import com.valensas.kyc_android.identitycamera.model.document.IdentityCard
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
    private var identityCard = IdentityCard()

    private var currentDocumentSet: DocumentItemSet? = driversLicence
    private var finalized = AtomicBoolean(false)


    private val textCaptureCallback = object : ITextCaptureService.Callback {
        override fun onRequestLatestFrame(buffer: ByteArray?) {
            changeBuffer.set(false)
            fillBuffer(buffer)
            changeBuffer.set(true)
        }


        override fun onFrameProcessed(
                lines: Array<out ITextCaptureService.TextLine>?,
                status: IRecognitionService.ResultStabilityStatus?,
                warning: IRecognitionService.Warning?) {

            println(status)

            val currentDocumentSet = currentDocumentSet
            val currentDocument = currentDocumentSet?.document

            if (currentDocumentSet == driversLicence) {
                if (status?.ordinal!! <= 3)
                    return
            } else if (currentDocumentSet == identityCard) {
                if (status?.ordinal!! < 3) {
                    return
                }
            }


            var terminate = false

            if (lines != null && lines.isNotEmpty() && currentDocument != null && !finalized.get()) {
                currentDocument.clearDocumentInfo()
                val texts = createListOfStrings(lines)

                for (text in texts) {
                    for (category in currentDocument.documentItems.keys) {
                        val item = currentDocument.documentItems[category]

                        if (!currentDocument.skipWords.contains(text))
                            item?.attemptToWrite(text)
                    }

                    terminate = currentDocument.shouldTerminate()
                }
                currentDocument.print()
                if (terminate) {
                    finalized.set(true)
                    identityCameraPresenter.frontTextScanSuccessful(currentDocumentSet)
                }
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
        currentBuffer?.size?.let { System.arraycopy(currentBuffer, 0, buffer, 0, it) }
        textCaptureService?.submitRequestedFrame(buffer)
    }

    fun setDocumentType(type: String) {
        when (type) {
            "ehh" -> {
                currentDocumentSet = driversLicence
            }

            "kim" -> {
                currentDocumentSet = identityCard
            }

            "pass" -> {

            }
        }
    }

    fun createListOfStrings(textLines: Array<out ITextCaptureService.TextLine>?): List<String> {
        val list = mutableListOf<String>()

        if (textLines == null)
            return list

        for (line in textLines) {
            val text = line.Text
            list.addAll(text.split(" "))
        }

        return list
    }
}