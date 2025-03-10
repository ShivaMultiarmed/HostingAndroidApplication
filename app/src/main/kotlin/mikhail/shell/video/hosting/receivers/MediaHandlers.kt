package mikhail.shell.video.hosting.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.session.MediaSession
import android.os.Build
import android.view.KeyEvent
import androidx.media3.common.Player
import dagger.hilt.android.EntryPointAccessors
import mikhail.shell.video.hosting.di.AudioReceiverEntryPoint
import javax.inject.Inject

class MediaBroadcastReceiver: BroadcastReceiver() {
    private var player: Player? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        if (player == null && context != null) {
            val entryPoint = EntryPointAccessors.fromApplication<AudioReceiverEntryPoint>(context.applicationContext)
            player = entryPoint.getPlayer()
        }
        when (intent?.action) {
            AudioManager.ACTION_AUDIO_BECOMING_NOISY ->  player?.pause()
        }
    }
}

class MediaHandler @Inject constructor(
    private val player: Player
): MediaSession.Callback() {
    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
        handleMediaAction(mediaButtonIntent)
        return super.onMediaButtonEvent(mediaButtonIntent)
    }
    private fun handleMediaAction(intent: Intent) {
        val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
        else intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
        if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
            val keyCode = keyEvent.keyCode
            when(keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY -> player.let { if (!it.isPlaying) it.play() }
                KeyEvent.KEYCODE_MEDIA_PAUSE -> player.let { if (it.isPlaying) it.pause() }
            }
        }
    }
}
