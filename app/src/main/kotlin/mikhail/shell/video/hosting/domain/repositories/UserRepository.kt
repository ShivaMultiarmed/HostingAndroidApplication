package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.user.RemoveUserError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User

interface UserRepository {
    suspend fun get(userId: Long): Result<User, Error>
    suspend fun edit(user: User, avatar: String?, avatarAction: EditAction): Result<User, Error>
    suspend fun remove(userId: Long): Result<Unit, RemoveUserError>
}