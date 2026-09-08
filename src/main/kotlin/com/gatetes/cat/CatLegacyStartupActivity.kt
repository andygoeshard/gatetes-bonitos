package com.gatetes.cat

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.wm.WindowManager
import javax.swing.JFrame
import javax.swing.JLayeredPane
import javax.swing.SwingUtilities

/**
 * Fallback para Android Studio 2026.1 donde ProjectActivity no se dispara.
 * Esta es la API clásica StartupActivity que sí corre en todos.
 */
class CatLegacyStartupActivity : StartupActivity {
    private val log = Logger.getInstance(CatLegacyStartupActivity::class.java)

    override fun runActivity(project: Project) {
        log.warn("[Gatetes] Legacy StartupActivity run para ${project.name}")
        // suscribir listener de builds
        try { CatBuildListener(project).subscribe() } catch (_: Exception) {}
        SwingUtilities.invokeLater { tryAttachWithRetry(project, 15) }
    }

    private fun tryAttachWithRetry(project: Project, attempts: Int) {
        try {
            log.warn("[Gatetes-Legacy] tryAttach attempts=$attempts")
            val frame = WindowManager.getInstance().getFrame(project)
            log.warn("[Gatetes-Legacy] frame=$frame")
            if (frame == null && attempts > 0) {
                javax.swing.Timer(400) { tryAttachWithRetry(project, attempts - 1) }.apply { isRepeats = false; start() }
                return
            }
            if (frame == null) {
                log.warn("[Gatetes-Legacy] frame null abort")
                return
            }
            attachCats(project)
        } catch (e: Exception) {
            log.warn("[Gatetes-Legacy] exception", e)
        }
    }

    private fun attachCats(project: Project) {
        log.warn("[Gatetes-Legacy] attachCats inicio")
        val rawFrame = WindowManager.getInstance().getFrame(project)
        val frame = rawFrame as? JFrame ?: run {
            log.warn("[Gatetes-Legacy] frame no es JFrame $rawFrame")
            return
        }
        if (frame.width <= 100 || frame.height <= 100) {
            javax.swing.Timer(600) { attachCats(project) }.apply { isRepeats = false; start() }
            return
        }
        val layeredPane = frame.rootPane.layeredPane
        if (layeredPane.components.any { it is CatOverlayPanel }) {
            log.warn("[Gatetes-Legacy] overlay ya existe")
            return
        }
        if (!CatManager.isEnabled) {
            log.warn("[Gatetes-Legacy] disabled")
            return
        }
        val overlay = CatOverlayPanel(maxCats = CatManager.catCount)
        fun updateBounds() {
            val w = layeredPane.width.takeIf { it > 0 } ?: frame.width
            val h = layeredPane.height.takeIf { it > 0 } ?: frame.height
            overlay.setBounds(0, 0, w, h)
        }
        updateBounds()
        val l = object : java.awt.event.ComponentAdapter() {
            override fun componentResized(e: java.awt.event.ComponentEvent) { updateBounds() }
        }
        layeredPane.addComponentListener(l)
        frame.addComponentListener(l)
        layeredPane.add(overlay, JLayeredPane.DRAG_LAYER)
        overlay.putClientProperty("cat.project", project)
        CatManager.register(overlay)
        overlay.start()
        overlay.revalidate()
        overlay.repaint()
        log.warn("[Gatetes-Legacy] overlay creado bounds=${overlay.bounds} cats=${CatManager.catCount}")
        try {
            com.intellij.notification.NotificationGroupManager.getInstance().getNotificationGroup("Gatetes")
                .createNotification("Gatetes: ${CatManager.catCount} gatitos \uD83D\uDC31", com.intellij.notification.NotificationType.INFORMATION)
                .notify(project)
        } catch (_: Exception) {}
    }
}
