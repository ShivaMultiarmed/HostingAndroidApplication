package mikhail.shell.video.hosting.domain.errors

import com.google.gson.annotations.SerializedName

class ValidationException(val error: Error): RuntimeException()

interface Error

data object UnexpectedError: Error {
    override fun toString() = "UnexpectedError"
}

class CompoundError<T: Error>(): Error {
    @SerializedName("errors") private val _errors: MutableList<T> = mutableListOf()
    constructor(errors: List<T>): this() {
        _errors.addAll(errors)
    }
    constructor(vararg errors: T): this() {
        _errors.addAll(errors)
    }
    val errors = _errors.toList()
    fun add(error: T) {
        _errors.add(error)
    }
    operator fun plus(otherCompoundError: CompoundError<T>): CompoundError<T> {
        return CompoundError(errors + otherCompoundError.errors)
    }
    fun isEmpty(): Boolean {
        return _errors.isEmpty()
    }
    fun isNotEmpty(): Boolean {
        return _errors.isNotEmpty()
    }
    fun contains(error: Error): Boolean {
        return _errors.contains(error)
    }
}

fun <T: Error> Error?.equivalentTo(error: T): Boolean {
    return if (this is CompoundError<*>)
        this.contains(error)
    else
        this == error
}
fun Error?.isEmpty(): Boolean {
    return if (this is CompoundError<*>)
        this.isEmpty()
    else
        this == null
}

fun Error.toCompound(): CompoundError<Error> {
    return CompoundError<Error>().also { it.add(this) }
}

fun Error?.isNotEmpty(): Boolean = !isEmpty()
