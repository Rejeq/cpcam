package com.rejeq.cpcam.feature.settings.endpoint.form.obs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ObsEndpointFormStateTest {

    @Test
    fun `parseObsConfigUrl should parse valid OBS WebSocket URL`() {
        val url = "obsws://192.168.1.101:4455/12345678"
        val result = parseObsConfigUrl(url)

        assertEquals("192.168.1.101", result?.url?.state)
        assertEquals(4455, result?.port?.state)
        assertEquals("12345678", result?.password?.state)
    }

    @Test
    fun `parseObsConfigUrl should handle URLs with extra spaces`() {
        val url = "  obsws://192.168.1.101:4455/12345678  "
        val result = parseObsConfigUrl(url)

        assertEquals("192.168.1.101", result?.url?.state)
        assertEquals(4455, result?.port?.state)
        assertEquals("12345678", result?.password?.state)
    }

    @Test
    fun `parseObsConfigUrl should return null for invalid protocol`() {
        val url = "http://192.168.1.101:4455/12345678"
        val result = parseObsConfigUrl(url)
        assertNull(result)
    }

    @Test
    fun `parseObsConfigUrl should return null for missing password`() {
        val url = "obsws://192.168.1.101:4455"
        val result = parseObsConfigUrl(url)
        assertNull(result)
    }

    @Test
    fun `parseObsConfigUrl should return null for missing port`() {
        val url = "obsws://192.168.1.101/12345678"
        val result = parseObsConfigUrl(url)
        assertNull(result)
    }

    @Test
    fun `parseObsConfigUrl should return null for invalid port number`() {
        val url = "obsws://192.168.1.101:invalid/12345678"
        val result = parseObsConfigUrl(url)
        assertNull(result)
    }

    @Test
    fun `parseObsConfigUrl should handle localhost`() {
        val url = "obsws://localhost:4455/12345678"
        val result = parseObsConfigUrl(url)

        assertEquals("localhost", result?.url?.state)
        assertEquals(4455, result?.port?.state)
        assertEquals("12345678", result?.password?.state)
    }

    @Test
    fun `parseObsConfigUrl should handle empty string`() {
        val url = ""
        val result = parseObsConfigUrl(url)
        assertNull(result)
    }

    @Test
    fun `parseObsConfigUrl should handle malformed URL`() {
        val url = "obsws://"
        val result = parseObsConfigUrl(url)
        assertNull(result)
    }
}
