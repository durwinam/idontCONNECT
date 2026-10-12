package ir.idontconnect.data.profile

import ir.idontconnect.core.model.ProfileProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileParserTest {
    private val parser = ProfileParser()

    @Test fun parsesVlessWithoutLoggingOrRewritingSecret() {
        val link = "vless://00000000-0000-0000-0000-000000000000@example.com:443?type=tcp&security=none#test"
        val result = parser.parse(link)
        assertTrue(result is ProfileParseResult.Success)
        assertEquals(ProfileProtocol.VLESS, (result as ProfileParseResult.Success).profile.protocol)
        assertEquals(link, result.profile.rawContent)
    }

    @Test fun parsesSubscriptionUrlAsSubscriptionOnly() {
        val result = parser.parse("https://example.com/subscription")
        assertTrue(result is ProfileParseResult.Success)
        assertEquals(ProfileProtocol.SUBSCRIPTION, (result as ProfileParseResult.Success).profile.protocol)
        assertTrue(result.profile.isSubscription)
    }

    @Test fun rejectsUnsupportedSchemes() {
        val result = parser.parse("ftp://example.com/config")
        assertEquals(ProfileParseResult.Reason.UNSUPPORTED_SCHEME, (result as ProfileParseResult.Failure).reason)
    }

    @Test fun rejectsJsonWithoutOutbounds() {
        val result = parser.parse("{"log":{}}")
        assertEquals(ProfileParseResult.Reason.INVALID_JSON, (result as ProfileParseResult.Failure).reason)
    }

    @Test fun rejectsEmptyInput() {
        val result = parser.parse("   ")
        assertEquals(ProfileParseResult.Reason.EMPTY, (result as ProfileParseResult.Failure).reason)
    }
}
