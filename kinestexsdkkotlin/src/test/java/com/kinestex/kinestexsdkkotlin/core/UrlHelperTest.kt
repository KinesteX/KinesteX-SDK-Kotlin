package com.kinestex.kinestexsdkkotlin.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class UrlHelperTest {
    @Test
    fun requireHttpsUrl_acceptsHttpsUrlsAndDropsTrailingSlash() {
        assertEquals(
            "https://ksa-ai.kinestex.com",
            UrlHelper.requireHttpsUrl("https://ksa-ai.kinestex.com/", "baseUrl")
        )
        assertEquals(
            "https://ksa-admin.kinestex.com/api/v1",
            UrlHelper.requireHttpsUrl(" https://ksa-admin.kinestex.com/api/v1/ ", "apiBaseUrl")
        )
    }

    @Test
    fun requireHttpsUrl_rejectsUrlsWithoutHttpsAndHost() {
        listOf(
            "",
            "ksa-ai.kinestex.com",
            "http://ksa-ai.kinestex.com",
            "https://",
            "https:///foo",
            "https://ksa ai.kinestex.com",
            "https://ksa-ai.kinestex.com?region=ksa",
            "https://ksa-ai.kinestex.com#top"
        ).forEach { value ->
            assertThrows(value, IllegalArgumentException::class.java) {
                UrlHelper.requireHttpsUrl(value, "baseUrl")
            }
        }
    }

    @Test
    fun originOf_keepsSchemeAndHostOnly() {
        assertEquals(
            "https://ksa-admin.kinestex.com",
            UrlHelper.originOf("https://ksa-admin.kinestex.com/api/v1/")
        )
        assertEquals(
            UrlHelper.DEFAULT_ADMIN_BASE_URL,
            UrlHelper.originOf("https://admin.kinestex.com/api/v1/")
        )
    }
}
