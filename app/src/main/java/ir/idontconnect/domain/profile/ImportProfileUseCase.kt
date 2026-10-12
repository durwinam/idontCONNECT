package ir.idontconnect.domain.profile

import ir.idontconnect.data.profile.ProfileRepository
import ir.idontconnect.data.profile.SaveProfileResult

/** Coordinates profile import without exposing persistence details to presentation code. */
class ImportProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(rawInput: String, preferredName: String? = null): SaveProfileResult =
        repository.import(rawInput, preferredName)
}
