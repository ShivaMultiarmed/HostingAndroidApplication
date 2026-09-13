package mikhail.shell.video.hosting.data.managers

import mikhail.shell.video.hosting.domain.managers.NotificationOrchestrator
import javax.inject.Inject
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class AndroidNotificationOrchestrator @Inject constructor() : NotificationOrchestrator {
    private var NOTIFCATION_ID = AtomicInt(0)
    override fun getId(): Int {
        return NOTIFCATION_ID.addAndFetch(0)
    }
    override fun incrementId(): Int {
        return NOTIFCATION_ID.addAndFetch(1)
    }
}