package mikhail.shell.video.hosting.domain.models

import mikhail.shell.video.hosting.domain.errors.Error

sealed class Result <out D, out E: Error> {
    data class Success<out D>(val data: D): Result<D, Nothing>()
    data class Failure<out E: Error>(val error: E): Result<Nothing, E>()
    fun onSuccess(action: (D) -> Unit): Result<D, E> {
        if (this is Success) {
            action(data)
        }
        return this
    }
    fun onFailure(action: (E) -> Unit): Result<D, E> {
        if (this is Failure) {
            action(error)
        }
        return this
    }
}