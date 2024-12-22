package mikhail.shell.video.hosting.data.dto

import mikhail.shell.video.hosting.domain.models.VideoDetails

data class VideoDetailsDto(
    val videoDto: VideoDto,
    val channelDto: ChannelDto,
)

fun VideoDetailsDto.toDomain(): VideoDetails {
    return VideoDetails(
        videoDto.toDomain(),
        channelDto.toDomain()
    )
}

fun VideoDetails.toDto(): VideoDetailsDto {
    return VideoDetailsDto(
        video.toDto(),
        channel.toDto()
    )
}