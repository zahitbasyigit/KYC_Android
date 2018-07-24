package com.valensas.kyc_android.identitycamera.model.document

import com.valensas.kyc_android.identitycamera.model.document.TextProperty.*


/**
 * Created by Zahit on 23-Jul-18.
 */
class DocumentItem(
        val format: String = "",
        var possiblePrefixes: List<String> = listOf<String>(),
        var possibleResults: MutableList<String> = mutableListOf<String>(),
        val finalAmountOfPossibleResults: Int,
        val indexOfOurResult: Int,
        var textProperty: TextProperty = DEFAULT
) {

    init {
        //Sort the list by length to avoid i.e. 4 replacing 4a first in deletePossiblePrefixes(text : String)
        possiblePrefixes = possiblePrefixes.sortedBy {
            -it.length
        }
    }

    fun attemptToWrite(text: String) {
        val fixedText = deletePossiblePrefixes(text)

        if (fitsFormat(fixedText) && !possibleResults.contains(fixedText)) {
            possibleResults.add(fixedText)
        }
    }

    private fun deletePossiblePrefixes(text: String): String {

        for (prefix in possiblePrefixes) {
            if (text.startsWith(prefix)) {
                return text.substring(prefix.length)
            }
        }
        return text
    }

    fun fitsFormat(text: String): Boolean {
        when (textProperty) {
            DEFAULT -> {

            }
            ALL_CAPS_TEXT -> {
                for (char in text) {
                    if (!char.isLetter() || char.isLowerCase()) return false
                }
            }
            FIRST_CAPS_TEXT -> {
                for (i in text.indices) {
                    if (i == 0 && (!text[0].isLetter() || text[0].isLowerCase())) return false
                    else if (i > 0 && (!text[i].isLetter() || text[i].isUpperCase())) return false
                }
            }
            ALL_LOWER_CASE_TEXT -> {
                for (char in text) {
                    if (!char.isLetter() || char.isUpperCase()) return false
                }
            }
        }

        if (format.isBlank()) { // No format is specified, true by default
            return true
        }

        if (text.length != format.length) //Format specified but lenghts do not match, false
            return false

        for (i in format.indices) {
            val formatChar = format[i]
            val textChar = text[i]
            when (formatChar) {
                'x' -> {
                    if (!textChar.isLetter() || !textChar.isLowerCase()) return false
                }

                'X' -> {
                    if (!textChar.isLetter() || textChar.isLowerCase()) return false
                }

                'n' -> {
                    if (!textChar.isDigit()) return false
                }

                else -> {
                    if (formatChar != textChar) return false
                }
            }
        }

        return true
    }
}

enum class TextProperty {
    DEFAULT,
    ALL_CAPS_TEXT,
    FIRST_CAPS_TEXT,
    ALL_LOWER_CASE_TEXT
}