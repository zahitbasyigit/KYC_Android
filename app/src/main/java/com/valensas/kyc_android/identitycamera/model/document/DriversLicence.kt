package com.valensas.kyc_android.identitycamera.model.document

/**
 * Created by Zahit on 23-Jul-18.
 */
class DriversLicence : DocumentItemSet() {

    init {
        build()
    }

    override fun build() {
        var skipSet = hashSetOf("TR", "SÜRÜCÜ", "BELGESİ", "DRIVING", "LICENCE", "TÜRKİYE", "CUMHURİYETİ")

        document = Document.Builder()
                .addCategory("surname", "", listOf("1. ", "1.", "1", ""), TextProperty.ALL_CAPS_TEXT, 1, 1)
                .addCategory("name", "", listOf("2. ", "2.", "2", "", "34 ", "34"), TextProperty.FIRST_CAPS_TEXT, 1, 3)
                .addCategory("birthdate", "nn.nn.nnnn", listOf("3. ", "3.", "3", "", "4a. ", "4a.", "4a", "4b. ", "4b.", "4b"), TextProperty.DEFAULT, 1, 3)
                .addCategory("tckn", "nnnnnnnnnnn", listOf("4d. ", "4d.", "d.", ""), TextProperty.DEFAULT, 1, 1)
                .addSkipwordList(skipSet)
                .build()
    }
}