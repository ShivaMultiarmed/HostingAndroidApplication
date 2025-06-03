package mikhail.shell.video.hosting.domain.errors

import com.google.gson.annotations.SerializedName

interface Error

class CompoundError<T: Error>(): Error {
    @SerializedName("errors") private val _errors: MutableList<T> = mutableListOf()
    constructor(errors: List<T>): this() {
        _errors.addAll(errors)
    }
    constructor(vararg errors: T): this() {
        _errors.addAll(errors)
    }
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
fun Error?.isNull(): Boolean {
    return if (this is CompoundError<*>)
        this.isNull()
    else
        this == null
}

fun Error.toCompound(): CompoundError<Error> {
    val error = this
    return CompoundError<Error>().apply { add(error) }
}

fun Error?.isNotNull(): Boolean = !this.isNull()
