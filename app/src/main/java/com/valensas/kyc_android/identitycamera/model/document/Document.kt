package com.valensas.kyc_android.identitycamera.model.document

/**
 * Created by Zahit on 23-Jul-18.
 */
class Document {
    val documentItems = mutableMapOf<String, DocumentItem>() //String(category) -> DocumentItem
    val skipWords = hashSetOf<String>()

    fun shouldTerminate(): Boolean {
        for (category in documentItems.keys) {
            val item = documentItems.get(category)
            if (item != null) {
                if (item.possibleResults.size < item.finalAmountOfPossibleResults) {
                    return false
                }
            }
        }
        return true
    }

    fun print() {
        var str = ""
        str += "Printing document!\n"
        for (category in documentItems.keys) {
            val item = documentItems[category]

            if (item != null) {
                str += "$category: "
                for (possibility in item.possibleResults) {
                    str += "$possibility,"
                }

                str += "!"

                if (item.possibleResults.size >= item.finalAmountOfPossibleResults)
                    str += "item ready and chosen: ${item.possibleResults[item.indexOfOurResult - 1]}"

                str += "\n"
            }

        }

        str += "---------------"
        println(str)
    }

    class Builder {
        private val document = Document()

        fun addCategory(category: String, format: String = "", possiblePrefixes: List<String>,
                        textProperty: TextProperty = TextProperty.DEFAULT, resultIndex: Int, totalResultCount: Int): Builder {
            document.documentItems.put(
                    category, DocumentItem(
                    format = format,
                    possiblePrefixes = possiblePrefixes,
                    textProperty = textProperty,
                    finalAmountOfPossibleResults = totalResultCount,
                    indexOfOurResult = resultIndex
            ))
            return this
        }

        fun addSkipword(word: String): Builder {
            document.skipWords.add(word)
            return this
        }

        fun addSkipwordList(wordList: HashSet<String>): Builder {
            document.skipWords.addAll(wordList)
            return this
        }

        fun build(): Document {
            return document
        }


    }
}
