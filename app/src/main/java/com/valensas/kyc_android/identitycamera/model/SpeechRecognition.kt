package com.valensas.kyc_android.identitycamera.model

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.valensas.kyc_android.identitycamera.IdentityCameraPresenter
import com.valensas.kyc_android.identitycamera.view.IdentityCameraActivity
import android.content.Intent
import android.speech.RecognizerIntent
import android.support.v4.app.ActivityCompat
import com.valensas.kyc_android.Manifest
import android.widget.Toast
import android.content.pm.PackageManager
import android.support.annotation.NonNull
import com.valensas.kyc_android.R


class SpeechRecognition( val identityCameraPresenter: IdentityCameraPresenter?): RecognitionListener {

    private val LOG_TAG = "SpeechRecognition"
    private lateinit var speech: SpeechRecognizer
    private lateinit var recognizerIntent: Intent


    fun recognizeSpeech() {
        //Toast.makeText(identityCameraPresenter?.getContext(), getString(R.string.speechRecognitionPromptText), Toast.LENGTH_LONG).show()
        speech = SpeechRecognizer.createSpeechRecognizer(identityCameraPresenter?.getContext());
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(identityCameraPresenter?.getContext()));
        speech.setRecognitionListener(this);
        recognizerIntent =  Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "tr");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                "tr");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        speech.startListening(recognizerIntent);

    }



    fun getErrorText(errorCode: Int): String {
        val message: String
        when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> message = "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> message = "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> message = "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> message = "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> message = "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> message = "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> message = "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> message = "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> message = "No speech input"
            else -> message = "Didn't understand, please try again."
        }
        return message
    }
    override fun onReadyForSpeech(p0: Bundle?) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    override fun onRmsChanged(rmsdB: Float) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    override fun onPartialResults(p0: Bundle?) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
        Log.i(LOG_TAG, "onEvent");
    }

    override fun onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
    }

    override fun onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
    }

    override fun onError(errorCode: Int) {
        val errorMessage = getErrorText(errorCode)
        Log.d(LOG_TAG, "FAILED $errorMessage")    }

    override fun onResults(results: Bundle?) {
        Log.i(LOG_TAG, "onResults")
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text = ""
        if (matches != null) {
            for (result in matches)
                text += result + "\n"
        }

       Log.i( LOG_TAG, text)

       var matched = false
        if (matches != null) {
            for (match in matches) {
                if (match.contains("KYC") || match.contains("beni")|| match.contains ("KYC") || match.contains("beni"))
                    matched = true
            }
                if(matched)
                    speech.stopListening();


            identityCameraPresenter?.speechRecognitionSuccessful(matches)

        }
    }
}