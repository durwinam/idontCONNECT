package ir.idontconnect.data.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ir.idontconnect.core.model.ServerProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "profiles")

sealed interface SaveProfileResult {
    data class Saved(val profile: ServerProfile) : SaveProfileResult
    data object Duplicate : SaveProfileResult
    data class Invalid(val reason: ProfileParseResult.Reason) : SaveProfileResult
}

class ProfileRepository(
    private val context: Context,
    private val parser: ProfileParser = ProfileParser(),
) {
    private val profilesKey = stringPreferencesKey("profiles_json_v1")
    private val selectedProfileKey = stringPreferencesKey("selected_profile_id")

    val selectedProfileId: Flow<String?> = context.profileDataStore.data.map { preferences ->
        preferences[selectedProfileKey] ?: decode(preferences[profilesKey].orEmpty()).firstOrNull()?.id
    }

    val profiles: Flow<List<ServerProfile>> = context.profileDataStore.data
        .map { preferences -> decode(preferences[profilesKey].orEmpty()) }

    suspend fun import(input: String, name: String? = null): SaveProfileResult {
        val parsed = parser.parse(input, name)
        if (parsed is ProfileParseResult.Failure) return SaveProfileResult.Invalid(parsed.reason)
        val profile = (parsed as ProfileParseResult.Success).profile
        val current = profiles.first()
        if (current.any { it.fingerprint() == profile.fingerprint() }) return SaveProfileResult.Duplicate
        var saved = false
        context.profileDataStore.edit { preferences ->
            val latest = decode(preferences[profilesKey].orEmpty())
            if (latest.none { it.fingerprint() == profile.fingerprint() }) {
                preferences[profilesKey] = encode(latest + profile)
                if (preferences[selectedProfileKey] == null) preferences[selectedProfileKey] = profile.id
                saved = true
            }
        }
        return if (saved) SaveProfileResult.Saved(profile) else SaveProfileResult.Duplicate
    }

    suspend fun remove(profileId: String) {
        context.profileDataStore.edit { preferences ->
            val remaining = decode(preferences[profilesKey].orEmpty()).filterNot { it.id == profileId }
            preferences[profilesKey] = encode(remaining)
            if (preferences[selectedProfileKey] == profileId) {
                val replacement = remaining.firstOrNull()?.id
                if (replacement == null) preferences.remove(selectedProfileKey)
                else preferences[selectedProfileKey] = replacement
            }
        }
    }

    suspend fun select(profileId: String) {
        context.profileDataStore.edit { preferences ->
            val exists = decode(preferences[profilesKey].orEmpty()).any { it.id == profileId }
            if (exists) preferences[selectedProfileKey] = profileId
        }
    }

    suspend fun setFavorite(profileId: String, favorite: Boolean) {
        context.profileDataStore.edit { preferences ->
            preferences[profilesKey] = encode(
                decode(preferences[profilesKey].orEmpty()).map { profile ->
                    if (profile.id == profileId) profile.copy(isFavorite = favorite) else profile
                },
            )
        }
    }

    suspend fun setFolder(profileId: String, folder: String) {
        val normalized = folder.trim().ifBlank { "Default" }.take(60)
        context.profileDataStore.edit { preferences ->
            preferences[profilesKey] = encode(
                decode(preferences[profilesKey].orEmpty()).map { profile ->
                    if (profile.id == profileId) profile.copy(folder = normalized) else profile
                },
            )
        }
    }

    private fun encode(profiles: List<ServerProfile>): String = JSONArray().apply {
        profiles.forEach { profile ->
            put(JSONObject().apply {
                put("id", profile.id)
                put("name", profile.name)
                put("protocol", profile.protocol.name)
                put("rawContent", profile.rawContent)
                put("isSubscription", profile.isSubscription)
                put("isFavorite", profile.isFavorite)
                put("folder", profile.folder)
                put("tags", JSONArray(profile.tags.toList()))
                put("createdAtEpochMs", profile.createdAtEpochMs)
                profile.lastSuccessfulConnectionEpochMs?.let { put("lastSuccessfulConnectionEpochMs", it) }
            })
        }
    }.toString()

    private fun decode(raw: String): List<ServerProfile> = runCatching {
        val array = JSONArray(raw)
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val protocol = runCatching {
                ir.idontconnect.core.model.ProfileProtocol.valueOf(item.optString("protocol"))
            }.getOrNull() ?: return@mapNotNull null
            ServerProfile(
                id = item.optString("id"),
                name = item.optString("name"),
                protocol = protocol,
                rawContent = item.optString("rawContent"),
                isSubscription = item.optBoolean(
                    "isSubscription",
                    protocol == ir.idontconnect.core.model.ProfileProtocol.SUBSCRIPTION,
                ),
                isFavorite = item.optBoolean("isFavorite"),
                folder = item.optString("folder", "Default"),
                tags = item.optJSONArray("tags")?.let { tags ->
                    (0 until tags.length()).map { index -> tags.optString(index) }.toSet()
                }.orEmpty(),
                createdAtEpochMs = item.optLong("createdAtEpochMs", 0L),
                lastSuccessfulConnectionEpochMs = if (item.has("lastSuccessfulConnectionEpochMs")) {
                    item.optLong("lastSuccessfulConnectionEpochMs")
                } else {
                    null
                },
            )
        }
    }.getOrDefault(emptyList())
}
