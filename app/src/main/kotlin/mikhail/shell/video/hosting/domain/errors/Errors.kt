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
    EMPTY,
    LARGE,
    NOT_SUPPORTED
}