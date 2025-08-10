package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.LikingState
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoWithUser
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class RateVideo @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(video: VideoWithUser, likingState: LikingState): Result<VideoWithUser, Error> {
        val result = repository.rateVideo(
            videoId = video.videoId!!,
            liking = likingState
        )
        return if (result is Result.Success<Unit>) {
            Result.Success(
                video.copy(
                    likes = video.likes.let { if (video.liking == LikingState.LIKED) it - 1 else it },
                    dislikes = video.dislikes.let { if (video.liking == LikingState.DISLIKED) it - 1 else it },
                    liking = likingState
                )
            )
        } else {
            result as Result.Failure<Error>
        }
    }
}