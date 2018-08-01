package com.valensas.kyc_android.identitycamera.model

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.valensas.kyc_android.identitycamera.IdentityCameraPresenter
import java.util.*


class SpeechRecognition(val identityCameraPresenter: IdentityCameraPresenter?) : RecognitionListener {

    private val LOG_TAG = "SpeechRecognition"
    var speech: SpeechRecognizer
    private var recognizerIntent: Intent
    private lateinit var requiredSpeech: String
    private var speechFactory = SpeechFactory()

    init {
        speech = SpeechRecognizer.createSpeechRecognizer(identityCameraPresenter?.getContext());
        speech.setRecognitionListener(this);
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "tr");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                "tr");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    fun recognizeSpeech() {
        requiredSpeech = speechFactory.createRandomSpeech()
        identityCameraPresenter?.speechRecognitionTextAvailable(requiredSpeech)
        speech.startListening(recognizerIntent);

        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(identityCameraPresenter?.getContext()));
    }


    private fun getErrorText(errorCode: Int): String {
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
        speech.cancel()

        val errorMessage = getErrorText(errorCode)
        Log.d(LOG_TAG, "FAILED $errorMessage")
        identityCameraPresenter?.speechRecognitionUnsuccessful(errorMessage)
    }

    override fun onResults(results: Bundle?) {
        speech.cancel()

        Log.i(LOG_TAG, "onResults")
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text = ""
        if (matches != null) {
            for (result in matches)
                text += result + " "
        }

        Log.i(LOG_TAG, text)

        if (matches != null && textsMatch(requiredSpeech, text)) {
            identityCameraPresenter?.speechRecognitionSuccessful(text)
        } else {
            identityCameraPresenter?.speechRecognitionUnsuccessful("Error, required : $requiredSpeech , found : $text")
        }


    }

    fun textsMatch(required: String, given: String): Boolean {
        return true
        //return calculate(required, given) < 1 * required.split(" ").size // 1 misspelling per word
    }

    fun calculate(x: String, y: String): Int {
        val dp = Array(x.length + 1) { IntArray(y.length + 1) }

        for (i in 0..x.length) {
            for (j in 0..y.length) {
                if (i == 0) {
                    dp[i][j] = j
                } else if (j == 0) {
                    dp[i][j] = i
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(x[i - 1], y[j - 1]),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1)
                }
            }
        }

        return dp[x.length][y.length]
    }

    private fun costOfSubstitution(a: Char, b: Char): Int {
        return if (a == b) 0 else 1
    }

    private fun min(vararg numbers: Int): Int {
        var min = Integer.MAX_VALUE

        for (number in numbers) {
            if (number < min) min = number
        }

        return min
    }
}