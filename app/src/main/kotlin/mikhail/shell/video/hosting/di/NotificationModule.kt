package mikhail.shell.video.hosting.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mikhail.shell.video.hosting.data.managers.AndroidNotificationOrchestrator
import mikhail.shell.video.hosting.domain.managers.NotificationOrchestrator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds
    @Singleton
    abstract fun bindNotificationOrchestrator(orchestrator: AndroidNotificationOrchestrator): NotificationOrchestrator
}