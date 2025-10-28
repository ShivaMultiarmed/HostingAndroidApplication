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
    NAME_NOT_VALID,
    NOT_FOUND,
    EMPTY,
    LARGE,
    NOT_SUPPORTED
}
enum class NumericError: Error {
    EMPTY,
    LOW,
    HIGH,
    NOT_EXISTS
}