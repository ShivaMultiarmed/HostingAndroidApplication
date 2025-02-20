package mikhail.shell.video.hosting.domain.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class PlayerService: Service() {
    @Inject
    lateinit var player: Player
    private val binder = PlayerBinder()
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }
    inner class PlayerBinder : Binder() {
        fun getService() = this@PlayerService
    }
    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}