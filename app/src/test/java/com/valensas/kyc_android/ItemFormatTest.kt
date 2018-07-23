package com.valensas.kyc_android

import com.valensas.kyc_android.identitycamera.model.document.Document
import com.valensas.kyc_android.identitycamera.model.document.DocumentItem
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Test
import java.util.*

/**
 * Created by Zahit on 23-Jul-18.
 */
class ItemFormatTest {

    @Test
    fun documentBirthdateTest() {

    }

    @Test
    fun documentTCKNTest() {

    }

    @Test
    fun documentItemTest() {
        assertEquals("BAŞYİĞİT".toLowerCase(Locale.forLanguageTag("tr")), "başyiğit")
    }

}