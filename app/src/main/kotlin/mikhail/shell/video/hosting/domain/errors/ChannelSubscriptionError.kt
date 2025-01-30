package mikhail.shell.video.hosting.domain.errors

enum class ChannelSubscriptionError: Error {
    SUBSCRIBING_FAILED, UNSUBSCRIBING_FAILED, RESUBSCRIBING_FAILED
}