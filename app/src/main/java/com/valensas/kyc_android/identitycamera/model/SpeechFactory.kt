package com.valensas.kyc_android.identitycamera.model

/**
 * Created by Zahit on 01-Aug-18.
 */
class SpeechFactory {
    private val subjects = listOf("Ben", "Sen", "O")
    private val nouns = listOf("Buraya", "Şuraya", "Oraya")
    private val verbs = listOf("Gel", "Git", "Otur")

    fun createRandomSpeech(): String {
        val firstRandom = (Math.random() * (subjects.size - 1)).toInt()
        val secondRandom = (Math.random() * (nouns.size - 1)).toInt()
        val thirdRandom = (Math.random() * (verbs.size - 1)).toInt()

        return subjects[firstRandom] + nouns[secondRandom] + verbs[thirdRandom]
    }
}