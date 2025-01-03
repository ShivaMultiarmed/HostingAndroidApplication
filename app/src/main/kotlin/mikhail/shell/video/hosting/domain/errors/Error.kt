package mikhail.shell.video.hosting.domain.errors

import com.google.gson.annotations.SerializedName

interface Error

data class CompoundError<T: Error>(
    @SerializedName("errors") private val _errors: MutableList<T> = mutableListOf()
): Error {
    //constructor(immutableErrors: List<T>): this(immutableErrors.toMutableList())
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
    fun contains(error: Error): Boolean {
        return _errors.contains(error)
    }
}

inline fun <reified T: Error> Error?.equivalentTo(error: T): Boolean {
    return if (this is CompoundError<*>)
        this.contains(error)
    else
        this == error
}
