package com.gatetes.cat

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.wm.WindowManager
import javax.swing.JFrame
import javax.swing.JLayeredPane
import javax.swing.SwingUtilities

class CatStartupActivity : ProjectActivity {
    private val log = Logger.getInstance(CatStartupActivity::class.java)

    override suspend fun execute(project: Project) {
        log.warn("[Gatetes] ProjectActivity execute para ${project.name}, isEnabled=${CatManager.isEnabled}, count=${CatManager.catCount}")
        // Notificación visible para debug
        try {
            NotificationGroupManager.getInstance().getNotificationGroup("Gatetes")
                .createNotification("Gatetes iniciando: ${CatManager.catCount} gatitos, enabled=${CatManager.isEnabled}", NotificationType.INFORMATION)
                .notify(project)
        } catch (_: Exception) {}
        // Reintentar hasta que el frame exista (puede tardar en inicializar)
        SwingUtilities.invokeLater { tryAttachWithRetry(project, attempts = 15) }
    }

    private fun tryAttachWithRetry(project: Project, attempts: Int) {
        try {
            log.warn("[Gatetes] tryAttachWithRetry attempts=$attempts")
            val frame = WindowManager.getInstance().getFrame(project)
            log.warn("[Gatetes] frame=$frame attempts=$attempts")
            if (frame == null && attempts > 0) {
                // reintentar en 400ms
                javax.swing.Timer(400) { tryAttachWithRetry(project, attempts - 1) }.apply {
                    isRepeats = false
                    start()
                }
                return
            }
            if (frame == null) {
                log.warn("[Gatetes] frame null después de reintentos, abort")
                return
            }
            attachCats(project)
        } catch (e: Exception) {
            log.warn("[Gatetes] exception en tryAttach", e)
            e.printStackTrace()
        }
    }

    private fun attachCats(project: Project) {
        log.warn("[Gatetes] attachCats inicio")
        val rawFrame = WindowManager.getInstance().getFrame(project)
        log.warn("[Gatetes] rawFrame=$rawFrame class=${rawFrame?.javaClass?.name}")
        val frame = rawFrame as? JFrame
        if (frame == null) {
            log.warn("[Gatetes] frame no es JFrame, abort. rawFrame=$rawFrame")
            try {
                NotificationGroupManager.getInstance().getNotificationGroup("Gatetes")
                    .createNotification("Gatetes error: frame no es JFrame: $rawFrame", NotificationType.ERROR)
                    .notify(project)
            } catch (_: Exception) {}
            return
        }
        log.warn("[Gatetes] frame size ${frame.width}x${frame.height} visible=${frame.isVisible} showing=${frame.isShowing}")
        // frame puede no estar visible aún; si no tiene tamaño, reintentar
        if (frame.width <= 100 || frame.height <= 100) {
            log.warn("[Gatetes] frame pequeño, reintentando en 600ms")
            javax.swing.Timer(600) { attachCats(project) }.apply { isRepeats = false; start() }
            return
        }
        val rootPane = frame.rootPane
        val layeredPane = rootPane.layeredPane

        // evitar duplicar si ya existe (ej. reopen project)
        for (c in layeredPane.components) {
            if (c is CatOverlayPanel) return
        }

        if (!CatManager.isEnabled) {
            log.warn("[Gatetes] CatManager.isEnabled=false, no se crea overlay")
            try {
                NotificationGroupManager.getInstance().getNotificationGroup("Gatetes")
                    .createNotification("Gatetes deshabilitado (Tools > Gatitos > Mostrar)", NotificationType.WARNING)
                    .notify(project)
            } catch (_: Exception) {}
            return
        }
        val overlay = CatOverlayPanel(maxCats = CatManager.catCount)

        // El overlay debe ocupar todo el frame. Usar frame size, no layeredPane.bounds que puede ser 0 al inicio.
        fun updateBounds() {
            val w = layeredPane.width.takeIf { it > 0 } ?: frame.width
            val h = layeredPane.height.takeIf { it > 0 } ?: frame.height
            overlay.setBounds(0, 0, w, h)
            overlay.revalidate()
            overlay.repaint()
        }
        updateBounds()
        // reajustar al resize de frame y layeredPane
        val resizeListener = object : java.awt.event.ComponentAdapter() {
            override fun componentResized(e: java.awt.event.ComponentEvent) { updateBounds() }
        }
        layeredPane.addComponentListener(resizeListener)
        frame.addComponentListener(resizeListener)

        // DRAG_LAYER está por encima de todo pero por debajo de popups modales.
        layeredPane.add(overlay, JLayeredPane.DRAG_LAYER)

        // registrar para dispose cuando se cierra el proyecto
        // usamos el disposable del proyecto para parar el timer
        // ProjectActivity no expone disposable directo, usamos un listener de cierre manual
        // Simple: overlay se detendrá solo cuando el frame se dispose.
        // Añadimos como cliente del rootPane para que se GC correctamente.
        overlay.putClientProperty("cat.project", project)
        CatManager.register(overlay)

        overlay.start()

        // forzar repaint inicial
        overlay.revalidate()
        overlay.repaint()
        log.warn("[Gatetes] overlay creado y started, bounds=${overlay.bounds}, cats=${CatManager.catCount}, layeredPane=${layeredPane.width}x${layeredPane.height}")
        try {
            NotificationGroupManager.getInstance().getNotificationGroup("Gatetes")
                .createNotification("Gatetes: ${CatManager.catCount} gatitos caminando \uD83D\uDC31 \u00A1Haz click para acariciar!", NotificationType.INFORMATION)
                .notify(project)
        } catch (_: Exception) {}
    }
}
