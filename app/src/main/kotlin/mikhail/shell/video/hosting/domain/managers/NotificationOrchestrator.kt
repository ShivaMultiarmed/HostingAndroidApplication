package mikhail.shell.video.hosting.domain.managers

interface NotificationOrchestrator {
    fun getId(): Int
    fun incrementId(): Int
}