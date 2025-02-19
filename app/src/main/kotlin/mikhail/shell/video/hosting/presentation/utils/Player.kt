package mikhail.shell.video.hosting.presentation.utils

import android.app.Activity
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
import android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun PlayerComponent(
    modifier: Modifier = Modifier,
    player: Player
) {
    var savedPlayState by rememberSaveable { mutableStateOf(false) }
    val pauseActions = {
        if (!(savedPlayState && !player.isPlaying))
            savedPlayState = player.isPlaying
        player.pause()
    }
    val playActions = {
        player.play()
    }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val windowDecorView = (context as Activity).window.decorView
    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(it).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                it.player = player
            }
        }
    )
    DisposableEffect(lifecycleOwner) {
        val audioManager = context.getSystemService(AudioManager::class.java)
        val audioListener = AudioManager.OnAudioFocusChangeListener {
            if (it == AUDIOFOCUS_LOSS_TRANSIENT) {
                pauseActions()
            } else if (it in arrayOf(AUDIOFOCUS_GAIN, AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK, AUDIOFOCUS_GAIN_TRANSIENT, AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)) {
                playActions()
            }
        }
        val audioFocusRequest = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(audioListener).build()
        audioManager.requestAudioFocus(audioFocusRequest)
//        val windowFocusListener = ViewTreeObserver.OnWindowFocusChangeListener {
//            if (!it) {
//                pauseActions()
//            } else if (savedPlayState) {
//                playActions()
//            }
//        }
//        windowDecorView.viewTreeObserver.addOnWindowFocusChangeListener(windowFocusListener)
        onDispose {
            audioManager.abandonAudioFocusRequest(audioFocusRequest)
            //lifecycleOwner.lifecycle.removeObserver(eventObserver)
            //windowDecorView.viewTreeObserver.removeOnWindowFocusChangeListener(windowFocusListener)
        }
    }
}