package mikhail.shell.video.hosting.domain.errors.channel

import mikhail.shell.video.hosting.domain.errors.Error

enum class ChannelSubscriptionError: Error {
    SUBSCRIBING_FAILED, UNSUBSCRIBING_FAILED, RESUBSCRIBING_FAILED
}