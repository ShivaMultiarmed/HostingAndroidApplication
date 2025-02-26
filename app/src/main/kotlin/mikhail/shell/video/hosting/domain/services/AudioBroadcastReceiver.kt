package mikhail.shell.video.hosting.domain.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.media3.common.Player
import dagger.hilt.android.EntryPointAccessors
import mikhail.shell.video.hosting.di.AudioReceiverEntryPoint

class AudioBroadcastReceiver: BroadcastReceiver() {
    private var player: Player? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        if (player == null && context != null) {
            val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, AudioReceiverEntryPoint::class.java)
            player = entryPoint.getPlayer()
        }
        when (intent?.action) {
            AudioManager.ACTION_AUDIO_BECOMING_NOISY ->  player?.pause()
        }
    }
}