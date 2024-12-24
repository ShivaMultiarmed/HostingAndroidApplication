package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.AuthError
import mikhail.shell.video.hosting.domain.models.AuthModel
import mikhail.shell.video.hosting.domain.models.Result

interface AuthRepository {
    suspend fun signInWithEmailAndPassword(
        email: String, password: String
    ): Result<AuthModel, AuthError>
}