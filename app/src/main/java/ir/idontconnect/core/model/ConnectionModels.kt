package ir.idontconnect.core.model

sealed interface EngineStatus {
    data object Unavailable : EngineStatus
    data object Disconnected : EngineStatus
    data object Connecting : EngineStatus
    data class Connected(val sinceEpochMs: Long) : EngineStatus
    data class Failed(val safeMessage: String) : EngineStatus
}

data class TrafficSample(
    val timestampEpochMs: Long,
    val downloadBytesPerSecond: Long,
    val uploadBytesPerSecond: Long,
)

data class NetworkHealth(
    val latencyMs: Long? = null,
    val jitterMs: Long? = null,
    val lastError: String? = null,
    val consecutiveFailures: Int = 0,
)
