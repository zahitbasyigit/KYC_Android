package com.valensas.kyc_android.identitycamera.model.document

/**
 * Created by Zahit on 25-Jul-18.
 */
abstract class DocumentItemSet {
    enum class Type {
        DRIVERS_LICENCE,
        IDENTITY_CARD,
        PASSPORT,
        NONE
    }

    var tckn = ""
    var name = ""
    var surname = ""
    var birthdate = ""
    var type = Type.NONE

    var document: Document? = null

    abstract fun build()

    fun finalizeDocument() {
        this.surname = loadStringFromDocument("surname")
        this.name = loadStringFromDocument("name")
        this.birthdate = loadStringFromDocument("birthdate")
        this.tckn = loadStringFromDocument("tckn")
    }

    fun loadStringFromDocument(category: String): String {
        val item = document?.documentItems?.get(category)
        if (item != null) {
            return item.possibleResults[item.indexOfOurResult - 1]
        }
        return ""
    }
}