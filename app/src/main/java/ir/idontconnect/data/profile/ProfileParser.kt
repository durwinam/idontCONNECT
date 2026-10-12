package ir.idontconnect.data.profile

import android.util.Base64
import ir.idontconnect.core.model.ProfileProtocol
import ir.idontconnect.core.model.ServerProfile
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import org.json.JSONObject

sealed interface ProfileParseResult {
    data class Success(val profile: ServerProfile) : ProfileParseResult
    data class Failure(val reason: Reason) : ProfileParseResult

    enum class Reason { EMPTY, UNSUPPORTED_SCHEME, MALFORMED_LINK, INVALID_JSON, DUPLICATE }
}

/** Strict, side-effect-free parser. It never logs or returns the submitted secret-bearing URI. */
class ProfileParser {
    fun parse(input: String, preferredName: String? = null): ProfileParseResult {
        val raw = input.trim()
        if (raw.isEmpty()) return ProfileParseResult.Failure(ProfileParseResult.Reason.EMPTY)

        if (raw.startsWith("{") || raw.startsWith("[")) {
            return parseJson(raw, preferredName)
        }

        val firstLine = raw.lineSequence().map(String::trim).firstOrNull(String::isNotEmpty)
            ?: return ProfileParseResult.Failure(ProfileParseResult.Reason.EMPTY)
        val scheme = firstLine.substringBefore(":", "").lowercase()
        val protocol = when (scheme) {
            "vless" -> ProfileProtocol.VLESS
            "vmess" -> ProfileProtocol.VMESS
            "trojan" -> ProfileProtocol.TROJAN
            "ss", "shadowsocks" -> ProfileProtocol.SHADOWSOCKS
            "https", "http" -> ProfileProtocol.SUBSCRIPTION
            else -> return ProfileParseResult.Failure(ProfileParseResult.Reason.UNSUPPORTED_SCHEME)
        }

        if (protocol == ProfileProtocol.SUBSCRIPTION) {
            val uri = runCatching { URI(firstLine) }.getOrNull()
            if (uri == null || uri.host.isNullOrBlank() || uri.scheme !in setOf("https", "http")) {
                return ProfileParseResult.Failure(ProfileParseResult.Reason.MALFORMED_LINK)
            }
            return ProfileParseResult.Success(
                ServerProfile(
                    name = preferredName?.takeIf(String::isNotBlank) ?: uri.host,
                    protocol = ProfileProtocol.SUBSCRIPTION,
                    rawContent = firstLine,
                    isSubscription = true,
                ),
            )
        }

        val parsed = when (protocol) {
            ProfileProtocol.VMESS -> parseVmess(firstLine)
            else -> runCatching {
                val uri = URI(firstLine)
                require(!uri.host.isNullOrBlank())
                require(uri.port in -1..65535)
                require(uri.port != 0)
                uri
            }.getOrNull()
        }
        if (parsed == null) return ProfileParseResult.Failure(ProfileParseResult.Reason.MALFORMED_LINK)

        val uriName = runCatching {
            URLDecoder.decode(firstLine.substringAfter('#', ""), StandardCharsets.UTF_8.name())
        }.getOrNull()?.takeIf(String::isNotBlank)
        val fallbackName = when (protocol) {
            ProfileProtocol.VLESS -> "VLESS profile"
            ProfileProtocol.VMESS -> "VMess profile"
            ProfileProtocol.TROJAN -> "Trojan profile"
            ProfileProtocol.SHADOWSOCKS -> "Shadowsocks profile"
            else -> "Profile"
        }
        return ProfileParseResult.Success(
            ServerProfile(
                name = preferredName?.trim()?.takeIf(String::isNotBlank) ?: uriName ?: fallbackName,
                protocol = protocol,
                rawContent = firstLine,
            ),
        )
    }

    private fun parseVmess(value: String): URI? {
        val encoded = value.removePrefix("vmess://")
        val decoded = runCatching {
            String(Base64.decode(encoded, Base64.DEFAULT), StandardCharsets.UTF_8)
        }.getOrNull() ?: return null
        val json = runCatching { JSONObject(decoded) }.getOrNull() ?: return null
        val host = json.optString("add").trim()
        val port = json.optString("port").toIntOrNull() ?: json.optInt("port", -1)
        if (host.isBlank() || port !in 1..65535 || json.optString("id").isBlank()) return null
        return URI("vmess", null, host, port, "/", null, null)
    }

    private fun parseJson(raw: String, preferredName: String?): ProfileParseResult {
        val root = runCatching { JSONObject(raw) }.getOrNull()
            ?: return ProfileParseResult.Failure(ProfileParseResult.Reason.INVALID_JSON)
        val outbounds = root.optJSONArray("outbounds")
        if (outbounds == null || outbounds.length() == 0) {
            return ProfileParseResult.Failure(ProfileParseResult.Reason.INVALID_JSON)
        }
        return ProfileParseResult.Success(
            ServerProfile(
                name = preferredName?.trim()?.takeIf(String::isNotBlank) ?: "Xray JSON profile",
                protocol = ProfileProtocol.XRAY_JSON,
                rawContent = raw,
            ),
        )
    }
}
