package mikhail.shell.video.hosting.domain.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.media3.common.Player

class PipPlayerReceiver (
    private val player : Player
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            it.action?.let {
                when (it) {
                    ACTION_PLAY -> player.play()
                    ACTION_PAUSE -> player.pause()
                    ACTION_FORWARD, ACTION_BACKWARD -> {
                        val diff = if (it == ACTION_FORWARD) 5000 else -5000
                        val newPos = (player.currentPosition + diff).coerceIn(0 until player.duration)
                        player.seekTo(newPos)
                    }
                    ACTION_CLOSE -> player.pause()
                }
            }
        }
    }
    companion object {
        private const val PACKAGE = "mikhail.shell.video.hosting"
        const val ACTION_CLOSE = "$PACKAGE.ACTION_CLOSE"
        const val CLOSE_CODE = 100
        const val ACTION_PLAY = "$PACKAGE.ACTION_PLAY"
        const val PLAY_CODE = 101
        const val ACTION_PAUSE = "$PACKAGE.ACTION_PAUSE"
        const val PAUSE_CODE = 102
        const val ACTION_FORWARD = "$PACKAGE.ACTION_FORWARD"
        const val FORWARD_CODE = 104
        const val ACTION_BACKWARD = "$PACKAGE.ACTION_BACKWARD"
        const val BACKWARD_CODE = 103
        val ACTIONS = listOf(ACTION_PLAY, ACTION_PAUSE, ACTION_FORWARD, ACTION_BACKWARD, ACTION_CLOSE)
    }
}