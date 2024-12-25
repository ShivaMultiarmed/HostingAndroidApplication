package mikhail.shell.video.hosting.domain.models

data class VideoDetails(
    val video: VideoWithUser,
    val channel: ChannelWithUser
)
