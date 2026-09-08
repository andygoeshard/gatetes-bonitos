package com.gatetes.cat

import com.intellij.openapi.Disposable
import java.awt.Cursor
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.JComponent
import javax.swing.Timer
import kotlin.random.Random

class CatOverlayPanel(private val maxCats: Int = 3) : JComponent(), Disposable {

    private val cats = mutableListOf<Cat>()
    private var timer: Timer? = null
    private var initialized = false
    private var draggedCat: Cat? = null
    private var draggedYarn: Boolean = false
    private var yarnDragPrevX: Int = 0
    private var yarnDragPrevY: Int = 0
    private var yarnDragVelX: Float = 0f
    private var yarnDragVelY: Float = 0f
    private var laserX: Int? = null
    private var laserY: Int? = null
    private var laserLife: Int = 0
    var yarn: YarnBall? = null
        private set
    private var scareUntil: Long = 0
    private var celebrateUntil: Long = 0
    private data class Paw(val x: Int, val y: Int, var life: Int = 180)
    private val paws = mutableListOf<Paw>()

    init {
        isOpaque = false
        isDoubleBuffered = true
        cursor = Cursor.getDefaultCursor()

        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                // pelotita arrastrable con física - prioridad
                yarn?.let { yb ->
                    if (yb.contains(e.x, e.y)) {
                        draggedYarn = true
                        yarnDragPrevX = e.x; yarnDragPrevY = e.y
                        yarnDragVelX = 0f; yarnDragVelY = 0f
                        // pausar física mientras se arrastra
                        yb.vx = 0f; yb.vy = 0f
                        e.consume(); repaint(); return
                    }
                }
                // Shift+Click = laser
                if (e.isShiftDown) {
                    laserX = e.x; laserY = e.y; laserLife = 90
                    cats.forEach { it.chase(e.x.toFloat(), e.y.toFloat()) }
                    repaint(); return
                }
                // buscar gato bajo cursor
                for (i in cats.indices.reversed()) {
                    val cat = cats[i]
                    if (cat.contains(e.x, e.y)) {
                        draggedCat = cat
                        cat.startDrag(e.x.toFloat(), e.y.toFloat())
                        cats.removeAt(i); cats.add(cat)
                        repaint(); e.consume(); return
                    }
                }
                if (e.button == 1 && e.clickCount == 2) {
                    laserX = e.x; laserY = e.y; laserLife = 70
                    cats.forEach { it.chase(e.x.toFloat(), e.y.toFloat()) }
                }
            }
            override fun mouseReleased(e: MouseEvent) {
                if (draggedYarn) {
                    yarn?.let { yb ->
                        // soltar con física: velocidad según arrastre
                        yb.vx = yarnDragVelX * 0.9f
                        yb.vy = yarnDragVelY * 0.9f
                        yb.rotSpeed = yarnDragVelX * 0.8f
                        // si fue solo click sin arrastre, dar impulso suave
                        if (kotlin.math.hypot(yarnDragVelX.toDouble(), yarnDragVelY.toDouble()) < 1.5) {
                            yb.vx += Random.nextFloat() * 6 - 3
                            yb.vy += Random.nextFloat() * 4 - 6
                        }
                        cats.forEach { it.chase(yb.x, yb.y) }
                    }
                    draggedYarn = false
                    repaint(); return
                }
                draggedCat?.let { cat ->
                    val moved = kotlin.math.hypot((cat.x + CatRenderer.DRAW_W/2 - e.x).toDouble(), (cat.y + CatRenderer.DRAW_H/2 - e.y).toDouble())
                    if (moved < 12) cat.pet() else cat.endDrag()
                    draggedCat = null
                    repaint()
                }
            }
            override fun mouseClicked(e: MouseEvent) {
                // pet ya manejado en pressed/released
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                val hovering = cats.any { it.contains(e.x, e.y) } || (laserX != null && laserLife > 0 && kotlin.math.hypot((e.x - laserX!!).toDouble(), (e.y - laserY!!).toDouble()) < 18)
                cursor = if (hovering) Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) else Cursor.getDefaultCursor()
            }
            override fun mouseDragged(e: MouseEvent) {
                if (draggedYarn) {
                    yarn?.let { yb ->
                        val dx = e.x - yarnDragPrevX
                        val dy = e.y - yarnDragPrevY
                        yarnDragVelX = dx.toFloat() * 0.9f
                        yarnDragVelY = dy.toFloat() * 0.9f
                        yb.x = e.x.toFloat()
                        yb.y = e.y.toFloat()
                        yb.x = yb.x.coerceIn(yb.radius.toFloat(), (width - yb.radius).toFloat())
                        yb.y = yb.y.coerceIn(yb.radius.toFloat(), (height - yb.radius - 8).toFloat())
                        yb.rot += dx * 0.6f
                        yarnDragPrevX = e.x; yarnDragPrevY = e.y
                        cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
                        repaint()
                    }
                    return
                }
                draggedCat?.let {
                    it.dragTo(e.x.toFloat(), e.y.toFloat())
                    it.x = it.x.coerceIn(0f, (width - CatRenderer.DRAW_W).toFloat().coerceAtLeast(0f))
                    it.y = it.y.coerceIn(0f, (height - CatRenderer.DRAW_H).toFloat().coerceAtLeast(0f))
                    cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
                    repaint()
                }
            }
        })

        // Alt+Click también dispara láser (más fácil que Shift en Mac)
        // Usamos AWT global listener para capturar Shift/Alt+Click aunque contains sea false
        try {
            val toolkit = java.awt.Toolkit.getDefaultToolkit()
            toolkit.addAWTEventListener({ event ->
                if (event is MouseEvent && event.id == MouseEvent.MOUSE_PRESSED && event.source is java.awt.Component) {
                    val comp = event.source as java.awt.Component
                    // solo si el click está dentro de este overlay (aunque contains false, el evento igual llega al toolkit)
                    val frame = javax.swing.SwingUtilities.getWindowAncestor(this)
                    if (frame != null && javax.swing.SwingUtilities.isDescendingFrom(comp, frame)) {
                        if (event.isShiftDown || event.isAltDown) {
                            // Convertir punto a coordenadas del overlay
                            val pt = javax.swing.SwingUtilities.convertPoint(comp, event.point, this)
                            if (pt.x in 0..width && pt.y in 0..height) {
                                javax.swing.SwingUtilities.invokeLater {
                                    laserX = pt.x; laserY = pt.y; laserLife = 90
                                    cats.forEach { it.chase(pt.x.toFloat(), pt.y.toFloat()) }
                                    repaint()
                                }
                            }
                        }
                    }
                }
            }, java.awt.AWTEvent.MOUSE_EVENT_MASK)
        } catch (_: Exception) {}
    }

    fun spawnYarn(x: Int = width / 2, y: Int = height / 3) {
        yarn = YarnBall(x.toFloat(), y.toFloat())
        cats.forEach { it.chase(x.toFloat(), y.toFloat()) }
        repaint()
    }

    fun clearYarn() {
        yarn = null
        repaint()
    }

    fun onBuildFailed() {
        scareUntil = System.currentTimeMillis() + 2800
        // todos huyen en direcciones opuestas
        cats.forEach {
            it.state = CatState.WALKING
            it.vx = 2.2f + Random.nextFloat() * 1.2f
            it.vy = 1.8f + Random.nextFloat()
            it.dirX = if (Random.nextBoolean()) 1 else -1
            it.dirY = if (Random.nextBoolean()) 1 else -1
            if (Random.nextFloat() < 0.6f) it.jump()
        }
    }

    fun onBuildSuccess() {
        celebrateUntil = System.currentTimeMillis() + 3000
        cats.forEach {
            it.state = CatState.SITTING
            it.stateTimer = 80 + Random.nextInt(60)
            it.hearts.add(HeartParticle(it.x + CatRenderer.DRAW_W/2f, it.y - 8f, vy = -1.2f, scale = 0.9f))
        }
    }

    override fun contains(x: Int, y: Int): Boolean {
        yarn?.let { if (it.contains(x, y)) return true }
        if (draggedYarn) return true
        if (laserX != null && laserLife > 0) {
            if (kotlin.math.hypot((x - laserX!!).toDouble(), (y - laserY!!).toDouble()) < 18) return true
        }
        if (draggedCat != null) return true
        return cats.any { it.contains(x, y) }
    }

    fun start() {
        if (timer != null) return
        ensureCats()
        timer = Timer(32) {
            if (!isShowing) return@Timer
            val w = width; val h = height
            if (w <= 0 || h <= 0) return@Timer
            if (laserLife > 0) laserLife-- else { laserX = null; laserY = null }
            yarn?.let { yb ->
                yb.update(w, h)
                if (!yb.isAlive()) { yarn = null } else {
                    cats.forEach { cat ->
                        val dist = kotlin.math.hypot((cat.x - yb.x).toDouble(), (cat.y - yb.y).toDouble())
                        if (dist < 280 && cat.state != CatState.DRAGGING && cat.state != CatState.SLEEPING) {
                            cat.chase(yb.x, yb.y)
                        }
                        if (dist < yb.radius + CatRenderer.DRAW_W/2.5) {
                            yb.hitByCat(cat)
                            cat.hearts.add(HeartParticle(cat.x + CatRenderer.DRAW_W/2f, cat.y - 6f, scale = 0.6f))
                        }
                    }
                }
            }
            // huellitas toggleables
            if (CatManager.pawPrintsEnabled) {
                cats.forEach { cat ->
                    if (cat.state == CatState.WALKING && cat.onGround && Random.nextFloat() < 0.08f) {
                        paws.add(Paw((cat.x + CatRenderer.DRAW_W/2).toInt(), (cat.y + CatRenderer.DRAW_H - 4).toInt()))
                        if (paws.size > 120) paws.removeAt(0)
                    }
                }
                paws.forEach { it.life-- }
                paws.removeIf { it.life <= 0 }
            } else {
                if (paws.isNotEmpty()) paws.clear()
            }
            cats.forEach { if (it.state != CatState.DRAGGING) it.update(w, h) }
            repaint()
        }.also { it.start() }
    }

    fun stop() { timer?.stop(); timer = null }

    fun recreateCats(newCount: Int) {
        // legacy: usa activeColors del manager
        recreateCatsWithColors(CatManager.activeColors.take(newCount))
    }

    fun recreateCatsWithColors(colors: List<CatColor>) {
        stop(); cats.clear(); initialized = false
        val w = if (width > 100) width else 1200
        val h = if (height > 100) height else 800
        // Si no hay colores (caso borde), usar fallback
        val list = if (colors.isEmpty()) listOf(CatColor.ORANGE) else colors
        list.forEachIndexed { idx, color ->
            val x = Random.nextInt(20, (w - CatRenderer.DRAW_W - 20).coerceAtLeast(100)).toFloat()
            val y = when {
                list.size <= 3 -> when (idx) {
                    0 -> (h - CatRenderer.DRAW_H - 40).toFloat() + Random.nextInt(-20, 20)
                    1 -> (h - CatRenderer.DRAW_H - 120).toFloat() + Random.nextInt(-30, 30)
                    else -> Random.nextInt(120, (h - CatRenderer.DRAW_H - 40).coerceAtLeast(200)).toFloat()
                }
                else -> Random.nextInt(20, (h - CatRenderer.DRAW_H - 20).coerceAtLeast(80)).toFloat()
            }
            cats.add(Cat(x = x, y = y.coerceIn(10f, (h - CatRenderer.DRAW_H - 10).toFloat()), color = color))
        }
        initialized = true; start(); repaint()
    }

    private fun ensureCats() {
        if (initialized) return
        initialized = true
        // Usar los colores activos del manager para que el menú refleje lo elegido
        val colors = CatManager.activeColors.takeIf { it.isNotEmpty() } ?: run {
            val shuffled = CatColor.values().toMutableList().shuffled()
            (0 until maxCats).map { shuffled[it % shuffled.size] }
        }
        val w = if (width > 100) width else 1200
        val h = if (height > 100) height else 800
        colors.forEachIndexed { idx, color ->
            val x = Random.nextInt(20, (w - CatRenderer.DRAW_W - 20).coerceAtLeast(100)).toFloat()
            val y = when {
                colors.size <= 3 -> when (idx) {
                    0 -> (h - CatRenderer.DRAW_H - 40).toFloat() + Random.nextInt(-20, 20)
                    1 -> (h - CatRenderer.DRAW_H - 120).toFloat() + Random.nextInt(-30, 30)
                    else -> Random.nextInt(120, (h - CatRenderer.DRAW_H - 40).coerceAtLeast(200)).toFloat()
                }
                else -> Random.nextInt(20, (h - CatRenderer.DRAW_H - 20).coerceAtLeast(80)).toFloat()
            }
            cats.add(Cat(x = x, y = y.coerceIn(10f, (h - CatRenderer.DRAW_H - 10).toFloat()), color = color))
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (cats.isEmpty()) ensureCats()
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)

        // huellitas debajo de todo
        if (CatManager.pawPrintsEnabled) {
            for (p in paws) {
                val alpha = (80 * (p.life.toFloat() / 180f)).toInt().coerceIn(0, 80)
                CatRenderer.drawPaw(g2, p.x, p.y, alpha)
            }
        }
        // pelotita
        yarn?.let { yb -> CatRenderer.drawYarn(g2, yb.x.toInt(), yb.y.toInt(), yb.rot, yb.life) }
        // laser
        laserX?.let { lx -> laserY?.let { ly ->
            if (laserLife > 0) CatRenderer.drawLaser(g2, lx, ly)
        } }

        for (cat in cats) {
            val scare = System.currentTimeMillis() < scareUntil
            val celebrate = System.currentTimeMillis() < celebrateUntil
            CatRenderer.draw(g2, cat.x.toInt(), cat.y.toInt(), cat, scare, celebrate)
            for (heart in cat.hearts) {
                val alpha = (255 * (heart.life.toFloat() / heart.maxLife)).toInt().coerceIn(0, 255)
                CatRenderer.drawHeart(g2, heart.x.toInt(), heart.y.toInt(), heart.scale, alpha)
            }
            for (zzz in cat.zzzs) {
                val alpha = (255 * (zzz.life.toFloat() / zzz.maxLife)).toInt().coerceIn(0, 160)
                CatRenderer.drawZzz(g2, zzz.x.toInt(), zzz.y.toInt(), alpha)
            }
            if (scare) {
                g2.color = java.awt.Color(0xFF, 0x60, 0x60, 180)
                g2.font = g2.font.deriveFont(9f)
                g2.drawString("!!", cat.x.toInt() + CatRenderer.DRAW_W/2 - 4, cat.y.toInt() - 4)
            }
        }
    }

    override fun dispose() { stop() }
}
