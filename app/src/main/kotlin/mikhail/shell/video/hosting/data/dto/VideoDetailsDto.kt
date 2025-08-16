package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.VideoWithChannelForUser

data class VideoDetailsDto(
    val video: VideoWithUserDto,
    val channel: ChannelWithUserDto,
)

fun VideoDetailsDto.toDomain(): VideoWithChannelForUser {
    return VideoWithChannelForUser(
        video.toDomain(),
        channel.toDomain()
    )
}

fun VideoWithChannelForUser.toDto(): VideoDetailsDto {
    return VideoDetailsDto(
        video.toDto(),
        channel.toDto()
    )
}