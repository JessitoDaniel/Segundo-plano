package org.godotengine.plugin.android.template

import android.content.Intent
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot

class GodotAndroidPlugin(godot: Godot) : GodotPlugin(godot) {

    override fun getPluginName() = "GodotAndroidPlugin"

    private fun enviar(accion: String, extras: Intent.() -> Unit = {}) {
        val ctx = activity ?: return
        val i = Intent(ctx, MusicService::class.java).setAction(accion).apply(extras)
        ctx.startForegroundService(i)
    }

    @UsedByGodot
    fun iniciar(lista: Array<String>, indice: Int) = enviar("INICIAR") {
        putStringArrayListExtra("lista", ArrayList(lista.toList()))
        putExtra("indice", indice)
    }

    @UsedByGodot fun alternarPausa() = enviar("PAUSA")
    @UsedByGodot fun siguiente() = enviar("SIGUIENTE")
    @UsedByGodot fun saltar(ms: Int) = enviar("SALTAR") { putExtra("ms", ms) }
    @UsedByGodot fun detener() = enviar("DETENER")

    @UsedByGodot fun nombreActual(): String = MusicService.nombreActual
    @UsedByGodot fun estaPausado(): Boolean = MusicService.pausado
    @UsedByGodot fun posicionMs(): Int = MusicService.instance?.posicionMs() ?: 0
}
