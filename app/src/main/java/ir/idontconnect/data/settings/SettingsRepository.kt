package ir.idontconnect.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

data class UserSettings(
    val languageTag: String = "system",
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val accentIndex: Int = 0,
    val glowIntensity: Float = 0.65f,
    val animationIntensity: Float = 0.70f,
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class SettingsRepository(private val context: Context) {
    private val languageKey = stringPreferencesKey("language_tag")
    private val themeKey = stringPreferencesKey("theme_mode")
    private val accentKey = intPreferencesKey("accent_index")
    private val glowKey = floatPreferencesKey("glow_intensity")
    private val animationKey = floatPreferencesKey("animation_intensity")

    val settings: Flow<UserSettings> = context.settingsDataStore.data.map { prefs ->
        UserSettings(
            languageTag = prefs[languageKey] ?: "system",
            theme = runCatching {
                ThemeMode.valueOf(prefs[themeKey] ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM),
            accentIndex = (prefs[accentKey] ?: 0).coerceIn(0, 3),
            glowIntensity = (prefs[glowKey] ?: 0.65f).coerceIn(0f, 1f),
            animationIntensity = (prefs[animationKey] ?: 0.70f).coerceIn(0f, 1f),
        )
    }

    suspend fun setLanguage(tag: String) = context.settingsDataStore.edit { it[languageKey] = tag }
    suspend fun setTheme(mode: ThemeMode) = context.settingsDataStore.edit { it[themeKey] = mode.name }
    suspend fun setAccent(index: Int) = context.settingsDataStore.edit { it[accentKey] = index.coerceIn(0, 3) }
    suspend fun setGlow(value: Float) = context.settingsDataStore.edit { it[glowKey] = value.coerceIn(0f, 1f) }
    suspend fun setAnimation(value: Float) = context.settingsDataStore.edit {
        it[animationKey] = value.coerceIn(0f, 1f)
    }
}
