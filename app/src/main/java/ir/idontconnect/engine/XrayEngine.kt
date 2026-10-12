package ir.idontconnect.engine

import ir.idontconnect.core.model.EngineStatus
import ir.idontconnect.core.model.ServerProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Boundary around the native engine. UI and data layers must not call native APIs directly. */
interface XrayEngine {
    val status: Flow<EngineStatus>
    suspend fun start(profile: ServerProfile): Result<Unit>
    suspend fun stop(): Result<Unit>
}

/** Safe default until a version-pinned libXray Android artifact is bundled and verified. */
class UnavailableXrayEngine : XrayEngine {
    override val status: Flow<EngineStatus> = flowOf(EngineStatus.Unavailable)

    override suspend fun start(profile: ServerProfile): Result<Unit> = Result.failure(
        EngineUnavailableException("Native Xray engine is not bundled in this source build."),
    )

    override suspend fun stop(): Result<Unit> = Result.success(Unit)
}

class EngineUnavailableException(message: String) : IllegalStateException(message)
