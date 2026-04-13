package com.rodkrtz.commonskit.core

import kotlin.test.Test
import kotlin.test.assertEquals

class StringsTest {
    @Test
    fun `removeBomAndInvisibleChars strips bom and zero-width characters`() {
        val raw = "\uFEFFhttps://sat.sef.sc.gov.br/nfce/consulta?p=abc\u200B\u200C\u200D\u2060"

        val sanitized = raw.removeBomAndInvisibleChars()

        assertEquals("https://sat.sef.sc.gov.br/nfce/consulta?p=abc", sanitized)
    }

    @Test
    fun `removeBomAndInvisibleChars returns same content when clean`() {
        val raw = "https://sat.sef.sc.gov.br/nfce/consulta?p=abc"

        val sanitized = raw.removeBomAndInvisibleChars()

        assertEquals(raw, sanitized)
    }
}
