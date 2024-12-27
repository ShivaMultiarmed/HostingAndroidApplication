package mikhail.shell.video.hosting.domain.errors

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

interface Error

data class CompoundError<T: Error>(
    @SerializedName("errors") private val _errors: MutableList<T> = mutableListOf()
): Error {
    //@Expose(serialize = true, deserialize = true)
    val errors: List<T>
        get() = _errors.toList()
    fun add(error: T) {
        _errors.add(error)
    }
    operator fun plus(compoundError: CompoundError<T>): CompoundError<T> {
        _errors.addAll(compoundError.errors)
        return this
    }
    fun isNull(): Boolean {
        return _errors.isEmpty()
    }
    fun isNotNull(): Boolean {
        return _errors.isNotEmpty()
    }
    fun contains(error: T): Boolean {
        return _errors.contains(error)
    }
}

fun <T: Error> CompoundError<T>?.contains(error: T): Boolean {
    return this?.contains(error) ?: false
}