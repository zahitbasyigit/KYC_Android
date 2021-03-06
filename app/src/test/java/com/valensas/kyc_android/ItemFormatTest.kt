package com.valensas.kyc_android

import com.valensas.kyc_android.identitycamera.model.document.Document
import com.valensas.kyc_android.identitycamera.model.document.TextProperty
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*

/**
 * Created by Zahit on 23-Jul-18.
 */
class ItemFormatTest {

    @Test
    fun arrayTest() {
        class myClass(val array: IntArray) {

        }

        val array = intArrayOf(2, 3, 4)
        val newArray = IntArray(3)
        System.arraycopy(array, 0, newArray, 0, 3)
        val item = myClass(newArray)
        array[0] = 15
        assertEquals(item.array[0], 2)


    }

    @Test
    fun documentBirthdateTest() {

    }

    @Test
    fun documentTCKNTest() {

    }

    @Test
    fun sortTest() {
        var stringList = listOf("4a", "4a.", "4", "4a. ", "")
        stringList = stringList.sortedBy { -it.length }
        assertEquals(stringList[0], "4a.")

    }

    @Test
    fun documentItemTest() {
        assertEquals("BAŞYİĞİT".toLowerCase(Locale.forLanguageTag("tr")), "başyiğit")

        val document = Document.Builder()
                .addCategory("surname", "", listOf("1. ", "1.", "1", ""), TextProperty.ALL_CAPS_TEXT, 1, 1)
                .addCategory("name", "", listOf("2. ", "2.", "2", "", "34 ", "34"), TextProperty.FIRST_CAPS_TEXT, 1, 3)
                .addCategory("birthdate", "nn.nn.nnnn", listOf("3. ", "3.", "3", "", "4a. ", "4a.", "4a", "4b. ", "4b.", "4b"), TextProperty.DEFAULT, 1, 3)
                .addCategory("tckn", "nnnnnnnnnnn", listOf("4d. ", "4d.", "d.", ""), TextProperty.DEFAULT, 1, 1)
                .build()

        //  assertEquals(document.documentItems[2].possibleResults[0], "26.02.2016")
    }

}