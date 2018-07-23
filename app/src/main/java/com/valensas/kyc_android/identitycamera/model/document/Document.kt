package com.valensas.kyc_android.identitycamera.model.document

/**
 * Created by Zahit on 23-Jul-18.
 */
class Document {
    val documentItems = mutableListOf<DocumentItem>()
    val skipWords = mutableListOf<String>()
    var certaintyThreshold = 0.8

    fun shouldTerminate(): Boolean {
        for (item in documentItems) {
            if (item.certainity < certaintyThreshold || item.possibleResults.size < item.finalAmountOfPossibleResults) {
                return false
            }
        }
        return true
    }

    fun print() {
        var str = ""
        str += "Printing document!\n"
        for (item in documentItems) {
            str += item.category + ": "
            for (possibility in item.possibleResults) {
                str += possibility + ","
            }

            str += "!"

            if (item.possibleResults.size == item.finalAmountOfPossibleResults)
                str += "item ready and chosen: ${item.possibleResults[item.indexOfOurResult - 1]}"

            str += "\n"
        }

        str += "---------------"
        println(str)
    }

    class Builder {
        val document = Document()

        fun addCategory(category: String, format: String = "", possiblePrefixes: List<String>,
                        textProperty: TextProperty = TextProperty.DEFAULT, resultIndex: Int, totalResultCount: Int): Builder {
            document.documentItems.add(DocumentItem(
                    category = category,
                    format = format,
                    possiblePrefixes = possiblePrefixes,
                    textPropery = textProperty,
                    finalAmountOfPossibleResults = totalResultCount,
                    indexOfOurResult = resultIndex
            ))
            return this
        }

        fun addSkipword(word: String): Builder {
            document.skipWords.add(word)
            return this
        }

        fun setSkipwordList(wordList: List<String>): Builder {
            document.skipWords.addAll(wordList)
            return this
        }

        fun setCertaintyThreshold(d: Double): Builder {
            document.certaintyThreshold = d
            return this
        }

        fun build(): Document {
            return document
        }


    }
}
