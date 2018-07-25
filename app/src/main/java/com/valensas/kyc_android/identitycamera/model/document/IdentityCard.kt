package com.valensas.kyc_android.identitycamera.model.document

/**
 * Created by Zahit on 25-Jul-18.
 */
class IdentityCard : DocumentItemSet() {

    init {
        build()
    }

    override fun build() {
        var skipSet = hashSetOf("TÜRKİYE", "CUMHURİYETİ", "KİMLİK", "KARTI", "REPUBLIC", "OF", "TURKEY", "IDENTITY", "CARD",
                "T.C.", "Kimlik", "No", "/", "TR", "Identity", "No", "Soyadı", "Surname", "Adı", "Given Name(s)", "Doğum")

        document = Document.Builder()
                .addCategory("surname", "", listOf(), TextProperty.ALL_CAPS_TEXT, 1, 2)
                .addCategory("name", "", listOf(), TextProperty.ALL_CAPS_TEXT, 2, 2)
                .addCategory("birthdate", "nn.nn.nnnn", listOf(), TextProperty.DEFAULT, 1, 2)
                .addCategory("tckn", "nnnnnnnnnnn", listOf(), TextProperty.DEFAULT, 1, 1)
                .addSkipwordList(skipSet)
                .build()
    }
}