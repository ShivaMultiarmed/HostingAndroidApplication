package mikhail.shell.video.hosting.presentation.player

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.util.Rational
import androidx.media3.common.Player
import mikhail.shell.video.hosting.R
import mikhail.shell.video.hosting.domain.receivers.PipPlayerReceiver

class PipPlayerUtils(
    private val context: Context,
    private val player: Player
) {
    private val backwardAction = createBackwardAction()
    private val playAction = createPlayAction()
    private val pauseAction = createPauseAction()
    private val forwardAction = createForwardAction()
    fun createPipParams(): PictureInPictureParams {
        val actions = listOf(
            backwardAction,
            if (player.isPlaying) pauseAction else playAction,
            forwardAction
        )
        return PictureInPictureParams.Builder()
            .setActions(actions)
            .setAspectRatio(Rational(player.videoSize.width, player.videoSize.height))
            .build()
    }
    private fun createPlayAction(): RemoteAction {
        val intent = Intent(PipPlayerReceiver.ACTION_PLAY)
        val pendingIntent = PendingIntent.getBroadcast(
            context, PipPlayerReceiver.PLAY_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val action = RemoteAction(
            Icon.createWithResource(context, R.drawable.baseline_play_arrow_24),
            context.getString(R.string.player_main_button_hint),
            "",
            pendingIntent
        )
        return action
    }

    private fun createPauseAction(): RemoteAction {
        val intent = Intent(PipPlayerReceiver.ACTION_PAUSE)
        val pendingIntent = PendingIntent.getBroadcast(
            context, PipPlayerReceiver.PAUSE_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val action = RemoteAction(
            Icon.createWithResource(context,R.drawable.baseline_pause_24),
            context.getString(R.string.player_main_button_hint),
            "",
            pendingIntent
        )
        return action
    }

    private fun createBackwardAction(): RemoteAction {
        val intent = Intent(PipPlayerReceiver.ACTION_BACKWARD)
        val pendingIntent = PendingIntent.getBroadcast(
            context, PipPlayerReceiver.BACKWARD_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val action = RemoteAction(
            Icon.createWithResource(
                context,
                R.drawable.backward_media_24
            ),
            context.getString(R.string.player_seek_backward_hint),
            "",
            pendingIntent
        )
        return action
    }

    private fun createForwardAction(): RemoteAction {
        val intent = Intent(PipPlayerReceiver.ACTION_FORWARD)
        val pendingIntent = PendingIntent.getBroadcast(
            context, PipPlayerReceiver.FORWARD_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val action = RemoteAction(
            Icon.createWithResource(
                context,
                R.drawable.forward_media_24
            ),
            context.getString(R.string.player_seek_forward_hint),
            "",
            pendingIntent
        )
        return action
    }
}