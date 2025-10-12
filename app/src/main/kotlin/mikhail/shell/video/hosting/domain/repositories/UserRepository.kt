package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.ImageSize
import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.NickCheckPurpose
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User
import mikhail.shell.video.hosting.domain.models.UserEditingModel

interface UserRepository {
    suspend fun get(userId: Long): Result<User, Error>
    suspend fun edit(user: UserEditingModel): Result<User, Error>
    suspend fun remove(): Result<Unit, Error>
    suspend fun existsByNick(purpose: NickCheckPurpose, nick: String): Result<Unit, Error>
    fun constructAvatarUrl(userId: Long, size: ImageSize): String
}