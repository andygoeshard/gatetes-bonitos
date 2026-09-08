package com.gatetes.cat

import com.intellij.ide.util.PropertiesComponent
import java.lang.ref.WeakReference

/**
 * Gestiona overlays y gatitos con colores individuales.
 * Soporta hasta 10 gatos, cada uno con color persistido.
 */
object CatManager {
    private const val KEY_ENABLED = "gatetes.enabled"
    private const val KEY_COUNT = "gatetes.count" // legacy
    private const val KEY_COLORS = "gatetes.colors" // ej: "ORANGE,WHITE,BLACK"
    private const val KEY_HATS = "gatetes.hats"
    private const val KEY_PAWS = "gatetes.paws"
    const val MAX_CATS = 10

    private val overlays = mutableListOf<WeakReference<CatOverlayPanel>>()

    var isEnabled: Boolean
        get() = PropertiesComponent.getInstance().getBoolean(KEY_ENABLED, true)
        set(value) {
            PropertiesComponent.getInstance().setValue(KEY_ENABLED, value, true)
            applyToAll()
        }

    // Legacy count, ahora derivado de activeColors.size
    var catCount: Int
        get() = activeColors.size.coerceIn(1, MAX_CATS)
        set(value) {
            val v = value.coerceIn(1, MAX_CATS)
            // ajustar lista al nuevo tamaño (ciclando colores)
            val current = activeColors.toMutableList()
            when {
                current.size < v -> {
                    val palette = CatColor.values().toList()
                    var idx = current.size
                    while (current.size < v) {
                        current.add(palette[idx % palette.size])
                        idx++
                    }
                }
                current.size > v -> {
                    while (current.size > v) current.removeAt(current.size - 1)
                }
            }
            activeColors = current
        }

    var activeColors: List<CatColor>
        get() {
            val raw = PropertiesComponent.getInstance().getValue(KEY_COLORS)
            if (raw.isNullOrBlank()) {
                // migrar desde count legacy
                val count = PropertiesComponent.getInstance().getInt(KEY_COUNT, 3).coerceIn(1, MAX_CATS)
                val palette = CatColor.values().toList()
                val list = (0 until count).map { palette[it % palette.size] }
                // guardar
                PropertiesComponent.getInstance().setValue(KEY_COLORS, list.joinToString(",") { it.name })
                return list
            }
            return raw.split(",").mapNotNull { try { CatColor.valueOf(it.trim()) } catch (_: Exception) { null } }.take(MAX_CATS).ifEmpty {
                listOf(CatColor.ORANGE, CatColor.WHITE, CatColor.BLACK)
            }
        }
        set(value) {
            val v = value.take(MAX_CATS)
            PropertiesComponent.getInstance().setValue(KEY_COLORS, v.joinToString(",") { it.name })
            // compatibilidad: actualizar count
            PropertiesComponent.getInstance().setValue(KEY_COUNT, v.size, 3)
            applyColorsToAll(v)
        }

    fun toggleColor(color: CatColor) {
        val current = activeColors.toMutableList()
        val idx = current.indexOf(color)
        if (idx >= 0) {
            // si hay más de un gato de ese color, quitar solo uno; si es el último, permitir quitar si no queda vacía
            if (current.size <= 1 && current.contains(color)) {
                // no permitir quedarse sin gatos, notificar
                return
            }
            current.removeAt(idx)
        } else {
            if (current.size >= MAX_CATS) return
            current.add(color)
        }
        activeColors = current
    }

    fun hasColor(color: CatColor): Boolean = activeColors.contains(color)
    fun countOf(color: CatColor): Int = activeColors.count { it == color }

    private fun applyColorsToAll(colors: List<CatColor>) {
        cleanUp()
        for (ref in overlays) ref.get()?.recreateCatsWithColors(colors)
    }

    private fun applyCountToAll(newCount: Int) {
        // legacy, ahora delega a activeColors
        cleanUp()
        for (ref in overlays) ref.get()?.recreateCats(newCount)
    }

    fun register(overlay: CatOverlayPanel) {
        cleanUp()
        overlays.add(WeakReference(overlay))
        overlay.isVisible = isEnabled
        if (isEnabled) overlay.start() else overlay.stop()
    }

    fun toggle() { isEnabled = !isEnabled }

    private fun applyToAll() {
        cleanUp()
        for (ref in overlays) {
            val o = ref.get() ?: continue
            o.isVisible = isEnabled
            if (isEnabled) o.start() else o.stop()
        }
    }

    fun isVisible(): Boolean = isEnabled

    private fun cleanUp() { overlays.removeIf { it.get() == null } }

    var hatsEnabled: Boolean
        get() = PropertiesComponent.getInstance().getBoolean(KEY_HATS, true)
        set(v) { PropertiesComponent.getInstance().setValue(KEY_HATS, v, true) }

    var pawPrintsEnabled: Boolean
        get() = PropertiesComponent.getInstance().getBoolean(KEY_PAWS, false)
        set(v) { PropertiesComponent.getInstance().setValue(KEY_PAWS, v, false) }

    fun overlayCount(): Int { cleanUp(); return overlays.count { it.get() != null } }

    fun spawnYarn() {
        cleanUp()
        for (ref in overlays) ref.get()?.spawnYarn()
    }

    fun clearYarn() {
        cleanUp()
        for (ref in overlays) ref.get()?.clearYarn()
    }

    fun onBuildFailed() {
        cleanUp()
        for (ref in overlays) ref.get()?.onBuildFailed()
    }

    fun onBuildSuccess() {
        cleanUp()
        for (ref in overlays) ref.get()?.onBuildSuccess()
    }
}
