package mikhail.shell.video.hosting.domain.repositories

import mikhail.shell.video.hosting.domain.errors.CompoundError
import mikhail.shell.video.hosting.domain.errors.EditUserError
import mikhail.shell.video.hosting.domain.errors.GetUserError
import mikhail.shell.video.hosting.domain.errors.RemoveUserError
import mikhail.shell.video.hosting.domain.models.EditAction
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.User

interface UserRepository {
    suspend fun get(userId: Long): Result<User, GetUserError>
    suspend fun edit(user: User, avatar: String?, avatarAction: EditAction): Result<User, CompoundError<EditUserError>>
    suspend fun remove(userId: Long): Result<Unit, RemoveUserError>
}