package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.VideoDetails

data class VideoDetailsDto(
    val video: VideoWithUserDto,
    val channel: ChannelWithUserDto,
)

fun VideoDetailsDto.toDomain(): VideoDetails {
    return VideoDetails(
        video.toDomain(),
        channel.toDomain()
    )
}

fun VideoDetails.toDto(): VideoDetailsDto {
    return VideoDetailsDto(
        video.toDto(),
        channel.toDto()
    )
}