package com.gatetes.cat

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.wm.WindowManager
import javax.swing.JFrame
import javax.swing.JLayeredPane

class ToggleCatsAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    override fun update(e: AnActionEvent) {
        val enabled = CatManager.isEnabled
        e.presentation.text = if (enabled) "Ocultar Gatitos \uD83D\uDE3F" else "Mostrar Gatitos \uD83D\uDC31"
        e.presentation.description = "Muestra u oculta los gatitos"
    }
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        CatManager.toggle()
        if (CatManager.isEnabled && project != null) ensureOverlay(project)
    }
    private fun ensureOverlay(project: com.intellij.openapi.project.Project) {
        val frame = WindowManager.getInstance().getFrame(project) as? JFrame ?: return
        val layeredPane = frame.rootPane.layeredPane
        val hasOverlay = layeredPane.components.any { it is CatOverlayPanel }
        if (!hasOverlay) {
            val overlay = CatOverlayPanel(maxCats = CatManager.catCount)
            val w = layeredPane.width.takeIf { it > 0 } ?: frame.width
            val h = layeredPane.height.takeIf { it > 0 } ?: frame.height
            overlay.setBounds(0, 0, w, h)
            layeredPane.addComponentListener(object : java.awt.event.ComponentAdapter() {
                override fun componentResized(ev: java.awt.event.ComponentEvent) {
                    val ww = layeredPane.width.takeIf { it > 0 } ?: frame.width
                    val hh = layeredPane.height.takeIf { it > 0 } ?: frame.height
                    overlay.setBounds(0, 0, ww, hh)
                }
            })
            layeredPane.add(overlay, JLayeredPane.DRAG_LAYER)
            CatManager.register(overlay)
            overlay.start()
        }
    }
}

// Cantidad rápida 1..10
open class SetCatsCountAction(private val count: Int) : AnAction("Gatitos: $count") {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = CatManager.isEnabled
    }
    override fun actionPerformed(e: AnActionEvent) {
        CatManager.catCount = count
        com.intellij.notification.NotificationGroupManager.getInstance().getNotificationGroup("Gatetes")
            .createNotification("Gatitos: $count aplicados \uD83D\uDC31", com.intellij.notification.NotificationType.INFORMATION).notify(e.project)
    }
}
class SetCats1Action : SetCatsCountAction(1)
class SetCats2Action : SetCatsCountAction(2)
class SetCats3Action : SetCatsCountAction(3)
class SetCats4Action : SetCatsCountAction(4)
class SetCats5Action : SetCatsCountAction(5)
class SetCats6Action : SetCatsCountAction(6)
class SetCats7Action : SetCatsCountAction(7)
class SetCats8Action : SetCatsCountAction(8)
class SetCats9Action : SetCatsCountAction(9)
class SetCats10Action : SetCatsCountAction(10)

// Toggle por color - muestra gatito de ese color en el menú
abstract class ToggleCatColorAction(private val color: CatColor, private val emoji: String, private val label: String) : ToggleAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    override fun isSelected(e: AnActionEvent): Boolean = CatManager.hasColor(color)
    override fun setSelected(e: AnActionEvent, state: Boolean) {
        // ToggleAction llama setSelected con el nuevo estado deseado, pero nosotros usamos toggleColor que ya invierte
        // Para evitar doble toggle, sincronizamos:
        val has = CatManager.hasColor(color)
        if (state != has) {
            CatManager.toggleColor(color)
            val count = CatManager.countOf(color)
            val total = CatManager.activeColors.size
            com.intellij.notification.NotificationGroupManager.getInstance().getNotificationGroup("Gatetes")
                .createNotification("$emoji $label: ${if (state) "añadido" else "quitado"} ($count) — total $total", com.intellij.notification.NotificationType.INFORMATION).notify(e.project)
        }
    }
    override fun update(e: AnActionEvent) {
        super.update(e)
        val c = CatManager.countOf(color)
        e.presentation.text = "$emoji $label${if (c > 1) " x$c" else ""}"
        e.presentation.description = "Click para ${if (CatManager.hasColor(color)) "quitar" else "añadir"} gatito $label"
        e.presentation.isEnabled = CatManager.isEnabled
    }
}
class ToggleOrangeAction : ToggleCatColorAction(CatColor.ORANGE, "\uD83D\uDC31", "Naranja")
class ToggleWhiteAction : ToggleCatColorAction(CatColor.WHITE, "🤍", "Blanco")
class ToggleBlackAction : ToggleCatColorAction(CatColor.BLACK, "\uD83D\uDC08\u200D⬛", "Negro")
class ToggleGrayAction : ToggleCatColorAction(CatColor.GRAY, "🩶", "Gris")
class ToggleCalicoAction : ToggleCatColorAction(CatColor.CALICO, "🐈", "Calicó")
class ToggleCreamAction : ToggleCatColorAction(CatColor.CREAM, "🍦", "Crema")
class ToggleBrownAction : ToggleCatColorAction(CatColor.BROWN, "🤎", "Marrón")
class ToggleSiameseAction : ToggleCatColorAction(CatColor.SIAMESE, "😺", "Siamés")
class ToggleTabbyAction : ToggleCatColorAction(CatColor.TABBY, "🐯", "Atigrado")
class ToggleTuxedoAction : ToggleCatColorAction(CatColor.TUXEDO, "🎩", "Smoking")

class ToggleHatsAction : ToggleAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    override fun isSelected(e: AnActionEvent): Boolean = CatManager.hatsEnabled
    override fun setSelected(e: AnActionEvent, state: Boolean) {
        CatManager.hatsEnabled = state
        com.intellij.notification.NotificationGroupManager.getInstance().getNotificationGroup("Gatetes")
            .createNotification("Gorritos ${if (state) "activados 🎩" else "desactivados"}", com.intellij.notification.NotificationType.INFORMATION).notify(e.project)
    }
    override fun update(e: AnActionEvent) {
        super.update(e); e.presentation.text = if (CatManager.hatsEnabled) "🎩 Gorritos ON" else "🎩 Gorritos OFF"
    }
}
class TogglePawsAction : ToggleAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    override fun isSelected(e: AnActionEvent): Boolean = CatManager.pawPrintsEnabled
    override fun setSelected(e: AnActionEvent, state: Boolean) {
        CatManager.pawPrintsEnabled = state
        com.intellij.notification.NotificationGroupManager.getInstance().getNotificationGroup("Gatetes")
            .createNotification("Huellitas ${if (state) "activadas 🐾" else "desactivadas"}", com.intellij.notification.NotificationType.INFORMATION).notify(e.project)
    }
    override fun update(e: AnActionEvent) {
        super.update(e); e.presentation.text = if (CatManager.pawPrintsEnabled) "🐾 Huellitas ON" else "🐾 Huellitas OFF"
    }
}

class SpawnYarnAction : AnAction("🧶 Lanzar pelotita", "Lanza una pelotita que todos persiguen", null) {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    override fun actionPerformed(e: AnActionEvent) {
        CatManager.spawnYarn()
        try {
            com.intellij.notification.NotificationGroupManager.getInstance().getNotificationGroup("Gatetes")
                .createNotification("🧶 ¡Pelotita lanzada!", com.intellij.notification.NotificationType.INFORMATION).notify(e.project)
        } catch (_: Exception) {}
    }
}
class SimulateBuildSuccessAction : AnAction("✅ Simular build OK", "Gatitos celebran panza arriba", null) {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    override fun actionPerformed(e: AnActionEvent) {
        CatManager.onBuildSuccess()
        com.intellij.notification.NotificationGroupManager.getInstance().getNotificationGroup("Gatetes")
            .createNotification("¡Build OK! Gatitos celebran 🎉", com.intellij.notification.NotificationType.INFORMATION).notify(e.project)
    }
}
class SimulateBuildFailAction : AnAction("❌ Simular build falló", "Gatitos asustados huyen", null) {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
    override fun actionPerformed(e: AnActionEvent) {
        CatManager.onBuildFailed()
        com.intellij.notification.NotificationGroupManager.getInstance().getNotificationGroup("Gatetes")
            .createNotification("¡Build falló! Gatitos asustados 😿", com.intellij.notification.NotificationType.WARNING).notify(e.project)
    }
}
