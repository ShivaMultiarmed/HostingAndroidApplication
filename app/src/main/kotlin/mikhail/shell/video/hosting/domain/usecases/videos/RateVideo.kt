package mikhail.shell.video.hosting.domain.usecases.videos

import mikhail.shell.video.hosting.domain.errors.Error
import mikhail.shell.video.hosting.domain.models.Liking
import mikhail.shell.video.hosting.domain.models.Result
import mikhail.shell.video.hosting.domain.models.VideoWithUser
import mikhail.shell.video.hosting.domain.repositories.VideoRepository
import javax.inject.Inject

class RateVideo @Inject constructor(
    private val repository: VideoRepository
) {
    suspend operator fun invoke(video: VideoWithUser, liking: Liking): Result<VideoWithUser, Error> {
        val result = repository.rateVideo(
            videoId = video.videoId!!,
            liking = liking
        )
        return if (result is Result.Success<Unit>) {
            Result.Success(
                video.copy(
                    likes = video.likes.let { if (video.liking == Liking.LIKED) it - 1 else it },
                    dislikes = video.dislikes.let { if (video.liking == Liking.DISLIKED) it - 1 else it },
                    liking = liking
                )
            )
        } else {
            result as Result.Failure<Error>
        }
    }
}