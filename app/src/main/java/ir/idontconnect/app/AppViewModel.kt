package ir.idontconnect.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.idontconnect.core.model.EngineStatus
import ir.idontconnect.core.model.ServerProfile
import ir.idontconnect.core.model.TrafficSample
import ir.idontconnect.data.profile.ProfileParseResult
import ir.idontconnect.data.profile.ProfileRepository
import ir.idontconnect.data.profile.SaveProfileResult
import ir.idontconnect.data.settings.SettingsRepository
import ir.idontconnect.data.settings.ThemeMode
import ir.idontconnect.data.settings.UserSettings
import ir.idontconnect.domain.profile.ImportProfileUseCase
import ir.idontconnect.engine.XrayEngine
import ir.idontconnect.telemetry.TrafficTelemetry
import ir.idontconnect.telemetry.UnavailableTrafficTelemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UiMessage {
    PROFILE_SAVED_ENGINE_PENDING,
    DUPLICATE_PROFILE,
    EMPTY_PROFILE_INPUT,
    UNSUPPORTED_PROFILE_SCHEME,
    MALFORMED_PROFILE_LINK,
    INVALID_XRAY_JSON,
    ADD_PROFILE_BEFORE_CONNECTING,
    ENGINE_UNAVAILABLE,
    FILE_TOO_LARGE_OR_EMPTY,
    FILE_READ_FAILED,
}

data class AppUiState(
    val profiles: List<ServerProfile> = emptyList(),
    val activeProfileId: String? = null,
    val settings: UserSettings = UserSettings(),
    val engineStatus: EngineStatus = EngineStatus.Unavailable,
    val trafficSamples: List<TrafficSample> = emptyList(),
    val message: UiMessage? = null,
)

class AppViewModel(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val xrayEngine: XrayEngine,
    private val trafficTelemetry: TrafficTelemetry = UnavailableTrafficTelemetry(),
    private val importProfileUseCase: ImportProfileUseCase = ImportProfileUseCase(profileRepository),
) : ViewModel() {
    private val transientMessage = MutableStateFlow<UiMessage?>(null)
    private val trafficSamples = MutableStateFlow<List<TrafficSample>>(emptyList())

    private val baseState = combine(
        profileRepository.profiles,
        settingsRepository.settings,
        xrayEngine.status,
        transientMessage,
        profileRepository.selectedProfileId,
    ) { profiles, settings, engineStatus, message, selectedProfileId ->
        AppUiState(
            profiles = profiles,
            activeProfileId = selectedProfileId?.takeIf { selectedId ->
                profiles.any { profile -> profile.id == selectedId }
            },
            settings = settings,
            engineStatus = engineStatus,
            message = message,
        )
    }

    val state: StateFlow<AppUiState> = combine(baseState, trafficSamples) { base, samples ->
        base.copy(trafficSamples = samples)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppUiState(),
    )

    init {
        viewModelScope.launch {
            trafficTelemetry.samples(intervalMs = 3_000L).collect { sample ->
                trafficSamples.value = (trafficSamples.value + sample).takeLast(MAX_TRAFFIC_SAMPLES)
            }
        }
    }

    fun importProfile(raw: String, name: String?) = viewModelScope.launch {
        transientMessage.value = when (val result = importProfileUseCase(raw, name)) {
            is SaveProfileResult.Saved -> UiMessage.PROFILE_SAVED_ENGINE_PENDING
            SaveProfileResult.Duplicate -> UiMessage.DUPLICATE_PROFILE
            is SaveProfileResult.Invalid -> result.reason.toUiMessage()
        }
    }

    fun removeProfile(profile: ServerProfile) = viewModelScope.launch {
        profileRepository.remove(profile.id)
    }

    fun setFavorite(profile: ServerProfile, favorite: Boolean) = viewModelScope.launch {
        profileRepository.setFavorite(profile.id, favorite)
    }

    fun selectProfile(profile: ServerProfile) = viewModelScope.launch {
        profileRepository.select(profile.id)
    }

    fun connect() = viewModelScope.launch {
        val current = state.value
        val selected = current.profiles.firstOrNull { it.id == current.activeProfileId }
            ?: current.profiles.firstOrNull()

        if (selected == null) {
            transientMessage.value = UiMessage.ADD_PROFILE_BEFORE_CONNECTING
            return@launch
        }

        val result = xrayEngine.start(selected)
        transientMessage.value = if (result.isFailure) UiMessage.ENGINE_UNAVAILABLE else null
    }

    fun setTheme(mode: ThemeMode) = viewModelScope.launch {
        settingsRepository.setTheme(mode)
    }

    fun setLanguage(tag: String) = viewModelScope.launch {
        settingsRepository.setLanguage(tag)
    }

    fun setAccent(index: Int) = viewModelScope.launch {
        settingsRepository.setAccent(index)
    }

    fun setGlow(value: Float) = viewModelScope.launch {
        settingsRepository.setGlow(value)
    }

    fun setAnimation(value: Float) = viewModelScope.launch {
        settingsRepository.setAnimation(value)
    }

    fun showMessage(message: UiMessage) {
        transientMessage.value = message
    }

    fun clearMessage() {
        transientMessage.value = null
    }

    private fun ProfileParseResult.Reason.toUiMessage(): UiMessage = when (this) {
        ProfileParseResult.Reason.EMPTY -> UiMessage.EMPTY_PROFILE_INPUT
        ProfileParseResult.Reason.UNSUPPORTED_SCHEME -> UiMessage.UNSUPPORTED_PROFILE_SCHEME
        ProfileParseResult.Reason.MALFORMED_LINK -> UiMessage.MALFORMED_PROFILE_LINK
        ProfileParseResult.Reason.INVALID_JSON -> UiMessage.INVALID_XRAY_JSON
        ProfileParseResult.Reason.DUPLICATE -> UiMessage.DUPLICATE_PROFILE
    }

    class Factory(
        private val profiles: ProfileRepository,
        private val settings: SettingsRepository,
        private val engine: XrayEngine,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
                return AppViewModel(profiles, settings, engine) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    private companion object {
        const val MAX_TRAFFIC_SAMPLES = 120
    }
}
