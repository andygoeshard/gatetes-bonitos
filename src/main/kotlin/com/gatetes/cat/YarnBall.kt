package com.gatetes.cat

import kotlin.math.hypot
import kotlin.random.Random

class YarnBall(
    var x: Float,
    var y: Float,
    var vx: Float = Random.nextFloat() * 6 - 3,
    var vy: Float = Random.nextFloat() * 6 - 3,
    var rot: Float = 0f,
    var rotSpeed: Float = Random.nextFloat() * 20 - 10
) {
    var life: Int = 600 // 20s a 30fps, luego desaparece
    val radius = 10

    fun update(panelW: Int, panelH: Int) {
        x += vx
        y += vy
        vy += 0.28f // gravedad suave
        vx *= 0.988f // fricción
        vy *= 0.988f
        rot += rotSpeed
        rotSpeed *= 0.99f

        // rebote
        if (x - radius < 0) { x = radius.toFloat(); vx = -vx * 0.78f; rotSpeed = -rotSpeed }
        if (x + radius > panelW) { x = (panelW - radius).toFloat(); vx = -vx * 0.78f; rotSpeed = -rotSpeed }
        if (y - radius < 0) { y = radius.toFloat(); vy = -vy * 0.72f }
        if (y + radius > panelH - 8) {
            y = (panelH - 8 - radius).toFloat()
            vy = -vy * 0.62f
            vx *= 0.88f
            if (hypot(vx.toDouble(), vy.toDouble()) < 1.2) {
                // rodando
                vy = 0f
                // pequeña aleatoriedad para que no se quede quieta
                if (Random.nextFloat() < 0.03f) vx += Random.nextFloat() * 2 - 1
            }
        }
        life--
        // si queda quieta mucho, la hacemos desaparecer antes
        if (hypot(vx.toDouble(), vy.toDouble()) < 0.15 && life > 120) life = 120
    }

    fun isAlive(): Boolean = life > 0

    fun hitByCat(cat: Cat) {
        // impulso desde gato
        val dx = x - (cat.x + CatRenderer.DRAW_W / 2f)
        val dy = y - (cat.y + CatRenderer.DRAW_H / 2f)
        val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat().coerceAtLeast(1f)
        vx += (dx / dist) * 4.5f + Random.nextFloat() * 2 - 1
        vy += (dy / dist) * 3.5f - 1.5f
        rotSpeed += Random.nextFloat() * 12 - 6
        life = (life + 40).coerceAtMost(600)
    }

    fun contains(px: Int, py: Int): Boolean = hypot((px - x).toDouble(), (py - y).toDouble()) < radius + 6
}
