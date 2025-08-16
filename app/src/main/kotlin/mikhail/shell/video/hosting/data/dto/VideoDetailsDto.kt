package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.VideoWithChannelForUser

data class VideoDetailsDto(
    val video: VideoWithUserDto,
    val channel: ChannelWithUserDto,
)

fun VideoDetailsDto.toDomain() = VideoWithChannelForUser(
    video = video.toDomain(),
    channel = channel.toDomain()
)


fun VideoWithChannelForUser.toDto() = VideoDetailsDto(
    video.toDto(),
    channel.toDto()
)