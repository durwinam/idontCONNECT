package ir.idontconnect.core.model

import java.util.UUID

enum class ProfileProtocol(val scheme: String) {
    VLESS("vless"),
    VMESS("vmess"),
    TROJAN("trojan"),
    SHADOWSOCKS("ss"),
    XRAY_JSON("json"),
    SUBSCRIPTION("subscription"),
}

data class ServerProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val protocol: ProfileProtocol,
    val rawContent: String,
    val isSubscription: Boolean = protocol == ProfileProtocol.SUBSCRIPTION,
    val isFavorite: Boolean = false,
    val folder: String = "Default",
    val tags: Set<String> = emptySet(),
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val lastSuccessfulConnectionEpochMs: Long? = null,
) {
    /** A stable local fingerprint used for duplicate detection; raw secrets are never logged. */
    fun fingerprint(): String {
        val canonicalContent = when (protocol) {
            ProfileProtocol.SUBSCRIPTION, ProfileProtocol.XRAY_JSON -> rawContent.trim()
            else -> rawContent.trim().substringBefore('#')
        }
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(canonicalContent.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
