package mikhail.shell.video.hosting.domain.errors

enum class TextError: Error {
    EMPTY,
    SHORT,
    LONG,
    EXISTS,
    NOT_EXISTS,
    NOT_CORRECT,
    NOT_VALID,
    PATTERN
}

enum class OptionError: Error {
    EMPTY
}

enum class FileError: Error {
    NOT_FOUND,
    EMPTY,
    LARGE,
    NOT_SUPPORTED,
    NOT_VALID
}
enum class NumericError: Error {
    EMPTY,
    LOW,
    HIGH,
    NOT_EXISTS
}