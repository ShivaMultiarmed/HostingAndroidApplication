package mikhail.shell.video.hosting.presentation.utils

import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun PlayerComponent(
    modifier: Modifier = Modifier,
    player: Player
) {
    val context = LocalContext.current
    var savedPlayState by rememberSaveable { mutableStateOf(player.isPlaying) }
    AndroidView(
        modifier = modifier
            .background(MaterialTheme.colorScheme.onBackground),
        factory = {
            PlayerView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        },
        update = { it.player = player }
    )
    DisposableEffect(Unit) {
        val audioManager = context.getSystemService(AudioManager::class.java)
        val audioListener = AudioManager.OnAudioFocusChangeListener {
            if (it == AUDIOFOCUS_LOSS_TRANSIENT) {
                if (player.isPlaying) {
                    savedPlayState = true
                    player.pause()
                } else {
                    savedPlayState = false
                }
            } else if (savedPlayState && it in arrayOf(
                    AUDIOFOCUS_GAIN,
                    AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
                    AUDIOFOCUS_GAIN_TRANSIENT,
                    AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
                )
            ) {
                player.play()
            }
        }
        val audioFocusRequest = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(audioListener).build()
        audioManager.requestAudioFocus(audioFocusRequest)

        onDispose {
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
        }
    }
}