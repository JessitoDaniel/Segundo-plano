package org.godotengine.plugin.android.template

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import java.io.File

class MusicService : Service() {
    companion object {
        var instance: MusicService? = null
        var nombreActual = ""
        var pausado = false
    }

    private var player: MediaPlayer? = null
    private var lista: List<String> = emptyList()
    private var indice = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        iniciarPrimerPlano()
        when (intent?.action) {
            "INICIAR" -> {
                lista = intent.getStringArrayListExtra("lista") ?: arrayListOf()
                indice = intent.getIntExtra("indice", 0)
                reproducir()
            }
            "PAUSA" -> alternarPausa()
            "SIGUIENTE" -> siguiente()
            "SALTAR" -> saltar(intent.getIntExtra("ms", 0))
            "DETENER" -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun reproducir() {
        if (lista.isEmpty()) return
        player?.release()
        player = MediaPlayer().apply {
            setDataSource(lista[indice])
            setOnCompletionListener { siguiente() }
            prepare()
            start()
        }
        nombreActual = File(lista[indice]).nameWithoutExtension
        pausado = false
        actualizarNotificacion()
    }

    private fun siguiente() {
        if (lista.isEmpty()) return
        indice = (indice + 1) % lista.size
        reproducir()
    }

    private fun alternarPausa() {
        val p = player ?: return
        if (p.isPlaying) {
            p.pause()
            pausado = true
        } else {
            p.start()
            pausado = false
        }
    }

    private fun saltar(ms: Int) {
        val p = player ?: return
        p.seekTo((p.currentPosition + ms).coerceIn(0, p.duration))
    }

    fun posicionMs(): Int = player?.currentPosition ?: 0

    private fun notificacion(): Notification {
        val canal = "musica"
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(
                NotificationChannel(canal, "Música", NotificationManager.IMPORTANCE_LOW)
            )
        return Notification.Builder(this, canal)
            .setContentTitle("Reproductor")
            .setContentText(nombreActual.ifEmpty { "Listo" })
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }

    private fun iniciarPrimerPlano() {
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(
                1, notificacion(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(1, notificacion())
        }
    }

    private fun actualizarNotificacion() {
        getSystemService(NotificationManager::class.java).notify(1, notificacion())
    }

    override fun onDestroy() {
        player?.release()
        instance = null
        super.onDestroy()
    }
}
