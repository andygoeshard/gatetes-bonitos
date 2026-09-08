package com.gatetes.cat

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

class CatBuildListener(private val project: Project) {
    private val log = Logger.getInstance(CatBuildListener::class.java)

    fun subscribe() {
        // Por ahora solo log, las acciones manuales Tools > Juegos > Simular build usan CatManager directo.
        // El hook automático a ProjectTask se añadira cuando tengamos la API exacta de 2025.3.5 vs 2026.1
        log.warn("[Gatetes] BuildListener suscrito (modo manual) para ${project.name}")
        try {
            NotificationGroupManager.getInstance().getNotificationGroup("Gatetes")
                .createNotification("Gatetes escuchando builds (usa Tools > Juegos para probar reacción)", NotificationType.INFORMATION)
                .notify(project)
        } catch (_: Exception) {}
    }
}
