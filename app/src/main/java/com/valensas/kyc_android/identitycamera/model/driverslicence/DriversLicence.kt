package com.valensas.kyc_android.identitycamera.model.driverslicence

import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import com.valensas.kyc_android.identitycamera.model.document.Document
import com.valensas.kyc_android.identitycamera.model.document.TextProperty

/**
 * Created by Zahit on 23-Jul-18.
 */
class DriversLicence(var document: Document = Document()) {
    var tckn = ""
    var name = ""
    var surname = ""
    var birthdate = ""


    init {
        var skipList = listOf("TR", "SÜRÜCÜ", "BELGESİ", "DRIVING", "LICENCE", "TÜRKİYE", "CUMHURİYETİ")

        document = Document.Builder()
                .setCertaintyThreshold(0.8)
                .addCategory("surname", "", listOf("1. ", "1.", "1", "."), TextProperty.ALL_CAPS_TEXT, 1, 1)
                .addCategory("name", "", listOf("2. ", "2.", "2", ".", "34 ", "34"), TextProperty.FIRST_CAPS_TEXT, 1, 3)
                .addCategory("birthdate", "nn.nn.nnnn", listOf("3. ", "3.", "3", ".", "4a. ", "4a.", "4a", "4b. ", "4b.", "4b"), TextProperty.DEFAULT, 1, 3)
                .addCategory("tckn", "nnnnnnnnnnn", listOf("4d. ", "4d.", "d.", "."), TextProperty.DEFAULT, 1, 1)
                .setSkipwordList(skipList)
                .build()
    }

    fun finalizeDocument() {
        val surnameItem = document.documentItems[0]
        this.surname = surnameItem.possibleResults[surnameItem.indexOfOurResult - 1]

        val nameItem = document.documentItems[1]
        this.name = nameItem.possibleResults[nameItem.indexOfOurResult - 1]

        val birthdayItem = document.documentItems[2]
        this.birthdate = birthdayItem.possibleResults[birthdayItem.indexOfOurResult - 1]

        val tcknItem = document.documentItems[3]
        this.tckn = tcknItem.possibleResults[tcknItem.indexOfOurResult - 1]


    }


}