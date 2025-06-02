package mikhail.shell.video.hosting.domain.validation

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.errors.equivalentTo

fun <T: Error> constructInfoMessage(error: T?, errorMessages: Map<T, String>): String? {
    for ((currentError, message) in errorMessages) {
        if (error.equivalentTo(currentError)) {
            return message
        }
    }
    return null
}