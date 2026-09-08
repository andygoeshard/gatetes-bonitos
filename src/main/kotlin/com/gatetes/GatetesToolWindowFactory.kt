package com.gatetes

import com.gatetes.cat.CatColor
import com.gatetes.cat.CatManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import java.awt.*
import javax.swing.*

class GatetesToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = GatetesPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }
    override fun shouldBeAvailable(project: Project) = true
}

class GatetesPanel(private val project: Project) : JPanel(BorderLayout()) {
    private var isRefreshing = false

    init {
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        layout = BorderLayout()
        add(buildMain(), BorderLayout.CENTER)
    }

    private fun buildMain(): JComponent {
        val main = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        // Header
        main.add(JLabel("😺 Gatetes Bonitos").apply {
            font = font.deriveFont(Font.BOLD, 18f)
            alignmentX = Component.LEFT_ALIGNMENT
        })
        main.add(JLabel("Pixel art por todo el IDE — acaricia, arrastra, láser y pelotita").apply {
            foreground = Color(0x66, 0x66, 0x66)
            font = font.deriveFont(11f)
            alignmentX = Component.LEFT_ALIGNMENT
        })
        main.add(Box.createVerticalStrut(10))
        main.add(JSeparator().apply { maximumSize = Dimension(Short.MAX_VALUE.toInt(), 1) })
        main.add(Box.createVerticalStrut(10))

        // Visibilidad + estado
        val statusLabel = JLabel()
        fun updateStatus() {
            val total = CatManager.activeColors.size
            val hats = if (CatManager.hatsEnabled) "🎩" else "—"
            val paws = if (CatManager.pawPrintsEnabled) "🐾" else "—"
            statusLabel.text = "Estado: ${if (CatManager.isEnabled) "visible" else "oculto"} • $total gatitos • $hats $paws"
        }
        updateStatus()
        val enabledCheck = JCheckBox("Mostrar gatitos en el IDE", CatManager.isEnabled).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            isSelected = CatManager.isEnabled
            addActionListener {
                CatManager.isEnabled = isSelected
                updateStatus()
                // si se habilita y no hay overlay, notificar
                if (isSelected) {
                    // el overlay se crea al abrir proyecto; si ya hay proyecto, intentar asegurar
                    // no hacemos nada, el usuario puede reabrir o usar Tools
                }
            }
        }
        main.add(enabledCheck)
        main.add(Box.createVerticalStrut(4))
        statusLabel.alignmentX = Component.LEFT_ALIGNMENT
        statusLabel.font = statusLabel.font.deriveFont(11f)
        statusLabel.foreground = Color(0x55, 0x55, 0xAA)
        main.add(statusLabel)
        main.add(Box.createVerticalStrut(12))

        // Cantidad 1-10 con botones que marcan activo
        main.add(sectionLabel("Cantidad (1-10) — click para cambiar"))
        val countPanel = JPanel(GridLayout(2, 5, 4, 4)).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(320, 70)
        }
        val countButtons = mutableListOf<JToggleButton>()
        val bg = ButtonGroup()
        for (i in 1..10) {
            val btn = JToggleButton("$i").apply {
                isSelected = CatManager.activeColors.size == i
                toolTipText = "$i gatitos"
                addActionListener {
                    CatManager.catCount = i
                    updateStatus()
                    // actualizar selección visual
                    countButtons.forEachIndexed { idx, b -> b.isSelected = (idx + 1 == i) }
                }
            }
            bg.add(btn); countButtons.add(btn); countPanel.add(btn)
        }
        main.add(countPanel)
        main.add(Box.createVerticalStrut(10))

        // Colores 10 únicos
        main.add(sectionLabel("Gatitos por color — click para añadir/quitar (✓ activo)"))
        val emojiMap = mapOf(
            CatColor.ORANGE to "🐱", CatColor.WHITE to "🤍", CatColor.BLACK to "🐈‍⬛",
            CatColor.GRAY to "🩶", CatColor.CALICO to "🐈", CatColor.CREAM to "🍦",
            CatColor.BROWN to "🤎", CatColor.SIAMESE to "😺", CatColor.TABBY to "🐯", CatColor.TUXEDO to "🎩"
        )
        val nameMap = mapOf(
            CatColor.ORANGE to "Naranja", CatColor.WHITE to "Blanco", CatColor.BLACK to "Negro",
            CatColor.GRAY to "Gris", CatColor.CALICO to "Calicó", CatColor.CREAM to "Crema",
            CatColor.BROWN to "Marrón", CatColor.SIAMESE to "Siamés", CatColor.TABBY to "Atigrado", CatColor.TUXEDO to "Smoking"
        )
        val colorsPanel = JPanel(GridLayout(0, 2, 4, 4)).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            maximumSize = Dimension(320, 160)
        }
        val colorButtons = mutableMapOf<CatColor, JToggleButton>()
        for (color in CatColor.values()) {
            val has = CatManager.hasColor(color)
            val cnt = CatManager.countOf(color)
            val txt = "${emojiMap[color]} ${nameMap[color]}${if (cnt > 1) " x$cnt" else ""}"
            val btn = JToggleButton(txt, has).apply {
                toolTipText = if (has) "Quitar uno ${nameMap[color]}" else "Añadir ${nameMap[color]}"
                addActionListener {
                    CatManager.toggleColor(color)
                    updateStatus()
                    // refrescar todos los botones de color
                    for (c in CatColor.values()) {
                        val b = colorButtons[c] ?: continue
                        val h = CatManager.hasColor(c)
                        val cc = CatManager.countOf(c)
                        b.isSelected = h
                        b.text = "${emojiMap[c]} ${nameMap[c]}${if (cc > 1) " x$cc" else ""}"
                    }
                    // actualizar cantidad selection
                    val sz = CatManager.activeColors.size
                    countButtons.forEachIndexed { idx, b -> b.isSelected = (idx + 1 == sz) }
                }
            }
            colorButtons[color] = btn
            colorsPanel.add(btn)
        }
        main.add(colorsPanel)
        main.add(Box.createVerticalStrut(10))

        // Extras toggleables
        main.add(sectionLabel("Extras"))
        val hatsCheck = JCheckBox("🎩 Gorritos (45% gatitos con sombrero)", CatManager.hatsEnabled).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            addActionListener { CatManager.hatsEnabled = isSelected; updateStatus() }
        }
        val pawsCheck = JCheckBox("🐾 Huellitas en el suelo (se desvanecen)", CatManager.pawPrintsEnabled).apply {
            alignmentX = Component.LEFT_ALIGNMENT
            addActionListener { CatManager.pawPrintsEnabled = isSelected; updateStatus() }
        }
        main.add(hatsCheck)
        main.add(pawsCheck)
        main.add(Box.createVerticalStrut(10))

        // Juegos
        main.add(sectionLabel("Juegos & física"))
        val yarnBtn = JButton("🧶 Lanzar pelotita").apply {
            alignmentX = Component.LEFT_ALIGNMENT
            toolTipText = "Aparece en el centro, rebota y todos la persiguen. ¡Arrástrala!"
            addActionListener { CatManager.spawnYarn() }
        }
        val clearYarnBtn = JButton("🧹 Quitar pelotita").apply {
            alignmentX = Component.LEFT_ALIGNMENT
            addActionListener { CatManager.clearYarn() }
        }
        val dragTip = JLabel("Tip: arrastra la pelotita y suéltala con impulso").apply {
            alignmentX = Component.LEFT_ALIGNMENT; foreground = Color.GRAY; font = font.deriveFont(11f)
        }
        val laserTip = JLabel("Tip: Shift+Click o Alt+Click → láser rojo").apply {
            alignmentX = Component.LEFT_ALIGNMENT; foreground = Color.GRAY; font = font.deriveFont(11f)
        }
        val scatterBtn = JButton("💨 Esparcir gatitos").apply {
            alignmentX = Component.LEFT_ALIGNMENT
            toolTipText = "Recrea posiciones aleatorias"
            addActionListener {
                // forzar recreate con mismos colores pero nuevas posiciones
                val colors = CatManager.activeColors
                CatManager.activeColors = colors // triggea recreate
            }
        }
        main.add(yarnBtn); main.add(Box.createVerticalStrut(4))
        main.add(dragTip); main.add(Box.createVerticalStrut(4))
        main.add(clearYarnBtn); main.add(Box.createVerticalStrut(4))
        main.add(scatterBtn); main.add(Box.createVerticalStrut(4))
        main.add(laserTip)
        main.add(Box.createVerticalStrut(8))

        // Build reacción
        main.add(sectionLabel("Reacción a builds"))
        val successBtn = JButton("✅ Simular build OK — celebran 🎉").apply {
            alignmentX = Component.LEFT_ALIGNMENT
            addActionListener { CatManager.onBuildSuccess() }
        }
        val failBtn = JButton("❌ Simular build falló — se asustan 😿").apply {
            alignmentX = Component.LEFT_ALIGNMENT
            addActionListener { CatManager.onBuildFailed() }
        }
        main.add(successBtn); main.add(Box.createVerticalStrut(4))
        main.add(failBtn)
        main.add(Box.createVerticalStrut(8))
        main.add(JLabel("Hook automático a Gradle/Build pendiente de API 261").apply {
            alignmentX = Component.LEFT_ALIGNMENT; foreground = Color.GRAY; font = font.deriveFont(11f)
        })

        main.add(Box.createVerticalGlue())

        // timer refresco estado
        Timer(600) {
            if (isRefreshing) return@Timer
            isRefreshing = true
            try {
                val sz = CatManager.activeColors.size
                countButtons.forEachIndexed { idx, b -> b.isSelected = (idx + 1 == sz) }
                for (c in CatColor.values()) {
                    val b = colorButtons[c] ?: continue
                    val h = CatManager.hasColor(c)
                    val cc = CatManager.countOf(c)
                    if (b.isSelected != h) b.isSelected = h
                    val nt = "${emojiMap[c]} ${nameMap[c]}${if (cc > 1) " x$cc" else ""}"
                    if (b.text != nt) b.text = nt
                }
                enabledCheck.isSelected = CatManager.isEnabled
                hatsCheck.isSelected = CatManager.hatsEnabled
                pawsCheck.isSelected = CatManager.pawPrintsEnabled
                updateStatus()
            } finally { isRefreshing = false }
        }.apply { isRepeats = true; start() }

        return JBScrollPane(main).apply { border = null }
    }

    private fun sectionLabel(t: String) = JLabel(t).apply {
        alignmentX = Component.LEFT_ALIGNMENT
        font = font.deriveFont(Font.BOLD, 12f)
        foreground = Color(0x33, 0x33, 0x33)
        border = BorderFactory.createEmptyBorder(6, 0, 4, 0)
    }
}
