package com.gatetes.cat

import kotlin.math.hypot
import kotlin.random.Random

enum class CatState { WALKING, SITTING, SLEEPING, STRETCHING, PETTED, DRAGGING, CHASING }

enum class CatColor {
    ORANGE,      // naranja clásico
    WHITE,       // blanco nieve
    BLACK,       // negro azabache
    GRAY,        // gris perla
    CALICO,      // tricolor
    CREAM,       // crema vainilla
    BROWN,       // marrón chocolate
    SIAMESE,     // siamés seal point
    TABBY,       // atigrado marrón
    TUXEDO       // smoking blanco/negro
}

enum class HatType { NONE, SANTA, PARTY, WITCH, BEANIE, BOW }

enum class EarType { POINTY, ROUND }

data class HeartParticle(
    var x: Float, var y: Float, var vy: Float = -1.8f,
    var life: Int = 45, var maxLife: Int = 45, var scale: Float = 0.7f
)

data class ZzzParticle(var x: Float, var y: Float, var vy: Float = -0.6f, var life: Int = 60, var maxLife: Int = 60)

class Cat(
    var x: Float,
    var y: Float,
    var vx: Float = 1.1f,
    var vy: Float = 0.85f,
    var dirX: Int = if (Random.nextBoolean()) 1 else -1,
    var dirY: Int = if (Random.nextBoolean()) 1 else -1,
    var state: CatState = CatState.WALKING,
    var walkFrame: Int = 0,
    var frameTick: Int = 0,
    var stateTimer: Int = Random.nextInt(120, 300),
    var petTimer: Int = 0,
    val color: CatColor = CatColor.values().random(),
    var chaseTargetX: Float? = null,
    var chaseTargetY: Float? = null,
    val hat: HatType = if (Random.nextFloat() < 0.45f) HatType.values().filter { it != HatType.NONE }.random() else HatType.NONE,
    val earType: EarType = when (color) {
        CatColor.SIAMESE, CatColor.TABBY, CatColor.BLACK, CatColor.TUXEDO, CatColor.BROWN -> EarType.POINTY
        else -> EarType.ROUND
    }
) {
    val hearts = mutableListOf<HeartParticle>()
    val zzzs = mutableListOf<ZzzParticle>()
    var draggingOffsetX = 0f
    var draggingOffsetY = 0f

    // Para salto: velocidad vertical instantánea
    var jumpVy: Float = 0f
    var onGround: Boolean = true

    fun bounds(): Pair<Int, Int> = Pair(CatRenderer.DRAW_W, CatRenderer.DRAW_H)

    fun contains(px: Int, py: Int): Boolean {
        return px >= x && px <= x + CatRenderer.DRAW_W && py >= y && py <= y + CatRenderer.DRAW_H
    }

    fun pet() {
        state = CatState.PETTED
        petTimer = 52
        stateTimer = 60
        repeat(Random.nextInt(2, 4)) {
            hearts.add(HeartParticle(x = x + CatRenderer.DRAW_W / 2f + Random.nextInt(-10, 10), y = y - 6f, vy = -1.5f - Random.nextFloat(), scale = 0.6f + Random.nextFloat() * 0.5f))
        }
    }

    fun startDrag(mouseX: Float, mouseY: Float) {
        state = CatState.DRAGGING
        draggingOffsetX = mouseX - x
        draggingOffsetY = mouseY - y
        hearts.clear()
    }

    fun dragTo(mouseX: Float, mouseY: Float) {
        x = mouseX - draggingOffsetX
        y = mouseY - draggingOffsetY
    }

    fun endDrag() {
        state = CatState.WALKING
        stateTimer = Random.nextInt(60, 140)
        // impulso aleatorio al soltar
        dirX = if (Random.nextBoolean()) 1 else -1
        dirY = if (Random.nextBoolean()) 1 else -1
        vx = 1.0f + Random.nextFloat() * 0.8f
        vy = 0.8f + Random.nextFloat() * 0.6f
        // 30% salto
        if (Random.nextFloat() < 0.3f) jump()
    }

    fun jump() {
        if (onGround) {
            jumpVy = -6f
            onGround = false
        }
    }

    fun chase(tx: Float, ty: Float) {
        chaseTargetX = tx
        chaseTargetY = ty
        state = CatState.CHASING
        stateTimer = 180
    }

    fun update(panelW: Int, panelH: Int) {
        hearts.forEach { it.y += it.vy; it.vy += 0.04f; it.life--; it.x += Random.nextFloat() * 0.6f - 0.3f }
        hearts.removeIf { it.life <= 0 }
        zzzs.forEach { it.y += it.vy; it.life--; it.x += kotlin.math.sin(it.life * 0.15f) * 0.4f }
        zzzs.removeIf { it.life <= 0 }

        // Física de salto
        if (!onGround) {
            y += jumpVy
            jumpVy += 0.55f // gravedad
            if (y + CatRenderer.DRAW_H >= panelH - 10) {
                y = (panelH - CatRenderer.DRAW_H - 10).toFloat()
                jumpVy = 0f
                onGround = true
                // polvo al caer no, simple
            }
        }

        when (state) {
            CatState.DRAGGING -> {
                frameTick++
                if (frameTick % 8 == 0) walkFrame = (walkFrame + 1) % 4
                return // posición la maneja el panel
            }
            CatState.CHASING -> {
                val tx = chaseTargetX
                val ty = chaseTargetY
                if (tx != null && ty != null) {
                    val dx = tx - (x + CatRenderer.DRAW_W / 2f)
                    val dy = ty - (y + CatRenderer.DRAW_H / 2f)
                    val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()
                    if (dist < 12f) {
                        chaseTargetX = null; chaseTargetY = null
                        state = CatState.SITTING; stateTimer = 70
                        repeat(2) { hearts.add(HeartParticle(x + CatRenderer.DRAW_W/2f, y - 8f, scale = 0.7f)) }
                        return
                    }
                    // mover hacia target más rápido
                    val speed = 2.2f
                    x += (dx / dist) * speed
                    y += (dy / dist) * speed
                    dirX = if (dx > 0) 1 else -1
                    frameTick++; if (frameTick % 5 == 0) walkFrame = (walkFrame + 1) % 4
                    // bounce si se va
                    clamp(panelW, panelH)
                    stateTimer--
                    if (stateTimer <= 0) { chaseTargetX = null; state = CatState.WALKING; stateTimer = Random.nextInt(150, 300) }
                    return
                } else {
                    state = CatState.WALKING
                }
            }
            CatState.PETTED -> {
                petTimer--; frameTick++; if (frameTick % 8 == 0) walkFrame = (walkFrame + 1) % 4
                if (petTimer <= 0) { state = CatState.SITTING; stateTimer = Random.nextInt(90, 160) }
                return
            }
            CatState.SLEEPING -> {
                stateTimer--; frameTick++
                if (frameTick % 18 == 0 && Random.nextFloat() < 0.7f) {
                    zzzs.add(ZzzParticle(x + CatRenderer.DRAW_W * 0.7f, y - 4f))
                }
                if (stateTimer <= 0 || Random.nextFloat() < 0.006f) {
                    // 50% estirar al despertar
                    if (Random.nextFloat() < 0.5f) { state = CatState.STRETCHING; stateTimer = 45 } else { state = CatState.WALKING; stateTimer = Random.nextInt(180, 380); randomizeDir() }
                }
                return
            }
            CatState.STRETCHING -> {
                stateTimer--
                if (stateTimer <= 0) { state = CatState.WALKING; stateTimer = Random.nextInt(180, 380); randomizeDir() }
                return
            }
            CatState.SITTING -> {
                stateTimer--; frameTick++
                if (frameTick % 22 == 0 && Random.nextFloat() < 0.3f) {
                    // bostecito ocasional
                }
                if (stateTimer <= 0) {
                    val r = Random.nextFloat()
                    when {
                        r < 0.35f -> { state = CatState.SLEEPING; stateTimer = Random.nextInt(220, 420) }
                        r < 0.55f -> { state = CatState.STRETCHING; stateTimer = 42 }
                        else -> { state = CatState.WALKING; stateTimer = Random.nextInt(180, 400); randomizeDir() }
                    }
                }
                return
            }
            CatState.WALKING -> {
                frameTick++; if (frameTick % 7 == 0) walkFrame = (walkFrame + 1) % 4
                // movimiento 2D libre por toda la pantalla
                if (onGround) {
                    x += vx * dirX
                    y += vy * dirY
                }
                // rebote 4 paredes
                if (x <= 2) { x = 2f; dirX = 1 }
                else if (x + CatRenderer.DRAW_W >= panelW - 2) { x = (panelW - CatRenderer.DRAW_W - 2).toFloat(); dirX = -1 }
                if (y <= 8) { y = 8f; dirY = 1 }
                else if (y + CatRenderer.DRAW_H >= panelH - 8 && onGround) { y = (panelH - CatRenderer.DRAW_H - 8).toFloat(); dirY = -1 }

                // ocasional salto aleatorio
                if (Random.nextFloat() < 0.004f && onGround) jump()

                stateTimer--
                if (stateTimer <= 0) {
                    val r = Random.nextFloat()
                    when {
                        r < 0.28f -> { state = CatState.SITTING; stateTimer = Random.nextInt(80, 200) }
                        r < 0.50f -> { dirX *= -1; dirY *= -1; stateTimer = Random.nextInt(150, 350) }
                        r < 0.70f -> { dirX *= -1; stateTimer = Random.nextInt(140, 300) }
                        r < 0.85f -> { dirY *= -1; stateTimer = Random.nextInt(140, 300) }
                        else -> { vx = 0.7f + Random.nextFloat() * 1.1f; vy = 0.6f + Random.nextFloat() * 0.9f; stateTimer = Random.nextInt(120, 280) }
                    }
                }
                if (Random.nextFloat() < 0.006f) dirX *= -1
                if (Random.nextFloat() < 0.005f) dirY *= -1
            }
        }
    }

    private fun randomizeDir() {
        dirX = if (Random.nextBoolean()) 1 else -1
        dirY = if (Random.nextBoolean()) 1 else -1
        vx = 0.8f + Random.nextFloat() * 0.9f
        vy = 0.6f + Random.nextFloat() * 0.8f
    }

    private fun clamp(panelW: Int, panelH: Int) {
        if (x < 2) x = 2f
        if (x + CatRenderer.DRAW_W > panelW - 2) x = (panelW - CatRenderer.DRAW_W - 2).toFloat()
        if (y < 8) y = 8f
        if (y + CatRenderer.DRAW_H > panelH - 8) y = (panelH - CatRenderer.DRAW_H - 8).toFloat()
    }
}
