package com.gatetes.cat

import java.awt.Color
import java.awt.Graphics2D

object CatRenderer {
    private val WHITE = Color.WHITE
    private val BLACK = Color(0x1A, 0x1A, 0x1A)
    private val PINK = Color(0xFF, 0x8F, 0xAB)
    private val DARK_PINK = Color(0xE0, 0x5A, 0x7A)

    data class Palette(val base: Color, val dark: Color, val light: Color)

    private val PALETTES = mapOf(
        CatColor.ORANGE to Palette(Color(0xFF, 0xB8, 0x6C), Color(0xE8, 0x8A, 0x30), Color(0xFF, 0xD8, 0xB0)),
        CatColor.WHITE to Palette(Color(0xFE, 0xFE, 0xFE), Color(0xD8, 0xDD, 0xE2), Color(0xFF, 0xFF, 0xFF)),
        CatColor.BLACK to Palette(Color(0x3A, 0x3A, 0x40), Color(0x1A, 0x1A, 0x1E), Color(0x6A, 0x6A, 0x70)),
        CatColor.GRAY to Palette(Color(0xB0, 0xB8, 0xC2), Color(0x7E, 0x88, 0x95), Color(0xE0, 0xE6, 0xED)),
        CatColor.CALICO to Palette(Color(0xFF, 0xAB, 0x73), Color(0xD8, 0x7A, 0x3A), Color(0xFF, 0xE0, 0xB8)),
        CatColor.CREAM to Palette(Color(0xFF, 0xF0, 0xC8), Color(0xE8, 0xC8, 0x90), Color(0xFF, 0xFF, 0xE8)),
        CatColor.BROWN to Palette(Color(0x8B, 0x5A, 0x2B), Color(0x5A, 0x35, 0x15), Color(0xC8, 0x8A, 0x60)),
        CatColor.SIAMESE to Palette(Color(0xE8, 0xDD, 0xC8), Color(0x5A, 0x45, 0x35), Color(0xFF, 0xF0, 0xE0)),
        CatColor.TABBY to Palette(Color(0xD8, 0xA0, 0x60), Color(0x8B, 0x5A, 0x30), Color(0xFF, 0xD0, 0x90)),
        CatColor.TUXEDO to Palette(Color(0x2E, 0x2E, 0x32), Color(0x0E, 0x0E, 0x10), Color(0xFF, 0xFF, 0xFF))
    )

    const val LOGICAL_W = 16
    const val LOGICAL_H = 16
    const val SCALE = 3
    const val DRAW_W = LOGICAL_W * SCALE
    const val DRAW_H = LOGICAL_H * SCALE

    fun draw(g: Graphics2D, x: Int, y: Int, cat: Cat, scared: Boolean = false, celebrate: Boolean = false) {
        val g2 = g.create() as Graphics2D
        try {
            g2.translate(x, y)
            // si está asustado, orejas hacia atrás y ojos bien abiertos
            val scaredOffset = if (scared) 1 else 0
            if (cat.dirX == -1) {
                g2.translate(DRAW_W.toDouble(), 0.0)
                g2.scale(-1.0, 1.0)
            }
            val palette = PALETTES[cat.color] ?: PALETTES[CatColor.ORANGE]!!
            // sombra (más grande si celebra)
            if (celebrate) {
                g2.color = Color(0, 0, 0, 25)
                g2.fillRoundRect(1 * SCALE, 13 * SCALE, 14 * SCALE, 2 * SCALE, 6, 6)
            } else {
                g2.color = Color(0, 0, 0, 35)
                g2.fillRoundRect(2 * SCALE, 13 * SCALE, 12 * SCALE, 2 * SCALE, 4, 4)
            }

            when {
                scared -> drawScared(g2, palette, cat)
                celebrate -> drawCelebrate(g2, palette, cat)
                cat.state == CatState.SLEEPING -> drawSleeping(g2, palette, cat)
                cat.state == CatState.STRETCHING -> drawStretching(g2, palette, cat)
                cat.state == CatState.SITTING || cat.state == CatState.PETTED -> {
                    val petted = cat.state == CatState.PETTED
                    drawSitting(g2, palette, petted, cat)
                }
                else -> drawWalking(g2, palette, cat)
            }
            // gorrito en todos los estados si está habilitado y no es NONE (evita duplicar en walking que ya lo dibuja)
            if (CatManager.hatsEnabled && cat.state != CatState.SLEEPING && cat.state != CatState.STRETCHING
                && cat.state != CatState.SITTING && cat.state != CatState.PETTED
                && !scared && !celebrate) {
                // walking ya dibujó hat, este es para otros estados
                // no-op, hats de walking ya manejados
            } else if (CatManager.hatsEnabled && cat.hat != HatType.NONE && (cat.state == CatState.SITTING || cat.state == CatState.PETTED || cat.state == CatState.SLEEPING || cat.state == CatState.STRETCHING || scared || celebrate)) {
                drawHat(g2, cat)
            }
        } finally {
            g2.dispose()
        }
    }

    private fun drawScared(g: Graphics2D, pal: Palette, cat: Cat) {
        val s = SCALE
        // pelo erizado: cola grande
        g.color = pal.base
        g.fillRect(-1 * s, 5 * s, 4 * s, 3 * s)
        g.fillRect(0 * s, 4 * s, 3 * s, 2 * s)
        g.color = pal.dark
        g.drawRect(-1 * s, 5 * s, 4 * s, 3 * s)
        // cuerpo arqueado
        g.color = pal.base
        g.fillRoundRect(2 * s, 6 * s, 9 * s, 5 * s, s, s)
        g.color = WHITE
        g.fillRect(4 * s, 8 * s, 5 * s, 3 * s)
        g.color = pal.dark
        g.drawRoundRect(2 * s, 6 * s, 9 * s, 5 * s, s, s)
        // patas tiesas
        g.color = pal.base
        g.fillRect(3 * s, 11 * s, 2 * s, 2 * s); g.fillRect(8 * s, 11 * s, 2 * s, 2 * s)
        g.color = BLACK
        g.drawRect(3 * s, 11 * s, 2 * s, 2 * s); g.drawRect(8 * s, 11 * s, 2 * s, 2 * s)
        // cabeza con orejas para atrás
        g.color = pal.base
        g.fillRect(9 * s, 2 * s, 6 * s, 5 * s)
        g.color = WHITE; g.fillRect(9 * s, 5 * s, 6 * s, 2 * s)
        g.color = pal.dark; g.drawRect(9 * s, 2 * s, 6 * s, 5 * s)
        // orejas atrás
        g.color = pal.base
        g.fillRect(8 * s, 2 * s, 2 * s, 1 * s); g.fillRect(14 * s, 2 * s, 2 * s, 1 * s)
        // ojos bien abiertos
        g.color = BLACK
        g.fillOval(10 * s + s/2, 3 * s + s/2, s + s/2 + 1, s + 1)
        g.fillOval(13 * s, 3 * s + s/2, s + s/2 + 1, s + 1)
        g.color = WHITE
        g.fillOval(10 * s + s/2 + 2, 3 * s + s/2 + 1, 1, 1)
        g.fillOval(13 * s + 1, 3 * s + s/2 + 1, 1, 1)
        g.color = PINK; g.fillRect(12 * s, 5 * s, s, s)
    }

    private fun drawCelebrate(g: Graphics2D, pal: Palette, cat: Cat) {
        val s = SCALE
        // panza arriba - celebración build OK
        g.color = pal.base
        g.fillRoundRect(3 * s, 7 * s, 10 * s, 5 * s, s*2, s*2)
        g.color = WHITE
        g.fillRoundRect(4 * s, 8 * s, 8 * s, 3 * s, s, s)
        // patitas al aire
        g.color = pal.base
        g.fillRect(4 * s, 6 * s, 2 * s, 2 * s); g.fillRect(9 * s, 6 * s, 2 * s, 2 * s)
        g.fillRect(4 * s, 11 * s, 2 * s, 1 * s); g.fillRect(9 * s, 11 * s, 2 * s, 1 * s)
        g.color = pal.dark
        g.drawRoundRect(3 * s, 7 * s, 10 * s, 5 * s, s*2, s*2)
        // cabeza
        g.color = pal.base
        g.fillRoundRect(9 * s, 3 * s, 6 * s, 5 * s, s*2, s*2)
        g.color = WHITE; g.fillRect(9 * s, 6 * s, 6 * s, 2 * s)
        g.color = pal.dark; g.drawRoundRect(9 * s, 3 * s, 6 * s, 5 * s, s*2, s*2)
        // ojos cerrados felices
        g.color = BLACK
        g.drawLine(10 * s + s/2, 5 * s, 11 * s, 5 * s + s/2); g.drawLine(11 * s, 5 * s + s/2, 11 * s + s/2, 5 * s)
        g.drawLine(13 * s, 5 * s, 13 * s + s/2, 5 * s + s/2); g.drawLine(13 * s + s/2, 5 * s + s/2, 14 * s, 5 * s)
        g.color = PINK; g.fillRect(12 * s, 6 * s, s, s)
        g.color = Color(0xFF, 0xD0, 0x20)
        g.fillOval(7 * s, 2 * s, s, s) // estrellita
    }

    private fun drawWalking(g: Graphics2D, pal: Palette, cat: Cat) {
        val s = SCALE
        val isPetted = cat.state == CatState.PETTED || cat.state == CatState.DRAGGING
        val tailOffset = when (cat.walkFrame) {
            0 -> -1; 1 -> 0; 2 -> 1; else -> 0
        }
        // cola
        g.color = pal.base
        g.fillRect(0 * s, 7 * s + tailOffset * s, 3 * s, 2 * s)
        g.fillRect(0 * s, 6 * s + tailOffset * s, 2 * s, 2 * s)
        g.color = pal.dark
        g.fillRect(0 * s, 7 * s + tailOffset * s, 3 * s, s)

        // cuerpo + patrones por raza
        g.color = pal.base
        g.fillRect(2 * s, 6 * s, 9 * s, 5 * s)
        // detalles según color
        when (cat.color) {
            CatColor.CALICO -> {
                g.color = pal.light; g.fillRect(4 * s, 8 * s, 5 * s, 3 * s)
                g.color = BLACK; g.fillRect(3 * s, 6 * s, 2 * s, 2 * s) // mancha negra
                g.color = Color.WHITE; g.fillRect(6 * s, 9 * s, 2 * s, 1 * s)
            }
            CatColor.TABBY -> {
                g.color = Color(0x8B, 0x5A, 0x30); // rayas atigradas
                g.fillRect(3 * s, 6 * s, 1 * s, 5 * s); g.fillRect(6 * s, 6 * s, 1 * s, 5 * s); g.fillRect(9 * s, 6 * s, 1 * s, 5 * s)
                g.color = WHITE; g.fillRect(4 * s, 8 * s, 5 * s, 3 * s)
            }
            CatColor.TUXEDO -> {
                g.color = WHITE; g.fillRect(4 * s, 7 * s, 5 * s, 4 * s) // pechito smoking
                g.color = Color.WHITE; g.fillRect(5 * s, 8 * s, 3 * s, 2 * s)
            }
            CatColor.SIAMESE -> {
                g.color = Color(0x5A, 0x45, 0x35); g.fillRect(2 * s, 6 * s, 2 * s, 5 * s) // extremidades oscuras
                g.fillRect(9 * s, 6 * s, 2 * s, 5 * s)
                g.color = WHITE; g.fillRect(4 * s, 8 * s, 5 * s, 3 * s)
            }
            CatColor.WHITE -> { g.color = Color(0xE8, 0xE8, 0xE8); g.fillRect(4 * s, 8 * s, 5 * s, 3 * s) }
            CatColor.CREAM -> { g.color = Color(0xFF, 0xFF, 0xE8); g.fillRect(4 * s, 8 * s, 5 * s, 3 * s) }
            else -> { g.color = WHITE; g.fillRect(4 * s, 8 * s, 5 * s, 3 * s) }
        }
        g.color = pal.dark
        g.drawRect(2 * s, 6 * s, 9 * s, 5 * s)

        // patas animadas
        val legSpread = if (cat.walkFrame == 1 || cat.walkFrame == 3) 1 else 0
        g.color = pal.base
        g.fillRect((3 + legSpread) * s, 11 * s, 2 * s, 2 * s)
        g.fillRect((8 - legSpread) * s, 11 * s, 2 * s, 2 * s)
        g.color = if (cat.color == CatColor.BLACK) pal.light else WHITE
        g.fillRect((3 + legSpread) * s, 12 * s, 2 * s, s)
        g.fillRect((8 - legSpread) * s, 12 * s, 2 * s, s)
        g.color = BLACK
        g.drawRect((3 + legSpread) * s, 11 * s, 2 * s, 2 * s)
        g.drawRect((8 - legSpread) * s, 11 * s, 2 * s, 2 * s)

        // cabeza
        g.color = pal.base
        // Siames: cara oscura seal point
        if (cat.color == CatColor.SIAMESE) {
            g.fillRect(9 * s, 2 * s, 6 * s, 5 * s)
            g.color = Color(0x5A, 0x45, 0x35)
            g.fillRect(9 * s, 2 * s, 6 * s, 2 * s) // máscara
            g.color = Color(0xE8, 0xDD, 0xC8)
            g.fillRect(9 * s, 5 * s, 6 * s, 2 * s)
        } else if (cat.color == CatColor.TUXEDO) {
            g.fillRect(9 * s, 2 * s, 6 * s, 5 * s)
            g.color = WHITE
            g.fillRect(10 * s, 4 * s, 4 * s, 3 * s) // mancha blanca cara
            g.fillRect(9 * s, 5 * s, 6 * s, 2 * s)
        } else {
            g.fillRect(9 * s, 2 * s, 6 * s, 5 * s)
            if (cat.color == CatColor.BLACK) {
                g.color = Color(0x4A, 0x4A, 0x4A)
                g.fillRect(9 * s, 5 * s, 6 * s, 2 * s)
            } else {
                g.color = WHITE
                g.fillRect(9 * s, 5 * s, 6 * s, 2 * s)
            }
        }
        g.color = pal.dark
        g.drawRect(9 * s, 2 * s, 6 * s, 5 * s)

        // orejas - triangulito para algunos, redondeadas para otros
        drawEars(g, pal, cat)

        // gorrito si está habilitado
        if (CatManager.hatsEnabled) drawHat(g, cat)

        // ojos
        if (isPetted) {
            g.color = BLACK
            g.drawLine(10 * s + s / 2, 4 * s, 11 * s, 4 * s + s / 2)
            g.drawLine(11 * s, 4 * s + s / 2, 11 * s + s / 2, 4 * s)
            g.drawLine(13 * s, 4 * s, 13 * s + s / 2, 4 * s + s / 2)
            g.drawLine(13 * s + s / 2, 4 * s + s / 2, 14 * s, 4 * s)
            g.color = PINK
            g.fillOval(9 * s, 5 * s, 2 * s, s)
            g.fillOval(13 * s + s / 2, 5 * s, 2 * s, s)
        } else {
            g.color = BLACK
            g.fillOval(10 * s + s / 2, 3 * s + s / 2, s + s / 2, s + s / 2)
            g.fillOval(13 * s, 3 * s + s / 2, s + s / 2, s + s / 2)
            g.color = WHITE
            g.fillOval(10 * s + s, 3 * s + s, s / 2, s / 2)
            g.fillOval(13 * s + s / 2, 3 * s + s, s / 2, s / 2)
            if (cat.color == CatColor.ORANGE || cat.color == CatColor.CALICO) {
                g.color = Color(0x6B, 0xC6, 0x4A)
                g.fillOval(10 * s + s, 3 * s + s / 2 + 1, s / 2, s / 2)
                g.fillOval(13 * s + s / 2 + 1, 3 * s + s / 2 + 1, s / 2, s / 2)
            }
        }
        g.color = if (isPetted) DARK_PINK else PINK
        g.fillRect(12 * s, 5 * s, s, s)
        g.color = BLACK
        g.drawRect(12 * s, 5 * s, s, s)
        g.drawLine(9 * s, 5 * s + s / 2, 8 * s, 5 * s + s / 2)
        g.drawLine(9 * s, 6 * s, 8 * s, 6 * s)
        g.drawLine(15 * s, 5 * s + s / 2, 16 * s, 5 * s + s / 2)
        g.drawLine(15 * s, 6 * s, 16 * s, 6 * s)

        // si está en aire (saltando), dibujar líneas de movimiento
        if (!cat.onGround) {
            g.color = Color(0, 0, 0, 40)
            g.fillOval(4 * s, 13 * s, 6 * s, s)
        }
    }

    private fun drawSitting(g: Graphics2D, pal: Palette, isPetted: Boolean, cat: Cat) {
        val s = SCALE
        g.color = pal.base
        g.fillRect(1 * s, 10 * s, 5 * s, 2 * s)
        g.fillRect(1 * s, 9 * s, 2 * s, 2 * s)
        g.color = pal.dark
        g.drawRect(1 * s, 10 * s, 5 * s, 2 * s)

        g.color = pal.base
        g.fillRect(4 * s, 6 * s, 7 * s, 6 * s)
        if (cat.color == CatColor.WHITE) {
            g.color = Color(0xE8, 0xE8, 0xE8); g.fillRect(5 * s, 8 * s, 5 * s, 4 * s)
        } else {
            g.color = WHITE; g.fillRect(5 * s, 8 * s, 5 * s, 4 * s)
        }
        g.color = pal.dark
        g.drawRect(4 * s, 6 * s, 7 * s, 6 * s)

        g.color = if (cat.color == CatColor.BLACK) pal.light else WHITE
        g.fillRect(5 * s, 11 * s, 2 * s, 2 * s)
        g.fillRect(8 * s, 11 * s, 2 * s, 2 * s)
        g.color = BLACK
        g.drawRect(5 * s, 11 * s, 2 * s, 2 * s)
        g.drawRect(8 * s, 11 * s, 2 * s, 2 * s)

        g.color = pal.base
        g.fillRect(9 * s, 2 * s, 6 * s, 5 * s)
        g.color = pal.dark
        g.drawRect(9 * s, 2 * s, 6 * s, 5 * s)
        g.color = WHITE
        g.fillRect(9 * s, 5 * s, 6 * s, 2 * s)

        g.color = pal.base
        g.fillRect(9 * s, 1 * s, 2 * s, 2 * s)
        g.fillRect(13 * s, 1 * s, 2 * s, 2 * s)
        g.color = PINK
        g.fillRect(9 * s + s / 2, 1 * s + s / 2, s, s)
        g.fillRect(13 * s + s / 2, 1 * s + s / 2, s, s)
        g.color = pal.dark
        g.drawRect(9 * s, 1 * s, 2 * s, 2 * s)
        g.drawRect(13 * s, 1 * s, 2 * s, 2 * s)

        if (isPetted) {
            g.color = BLACK
            g.drawLine(10 * s + s / 2, 4 * s, 11 * s, 4 * s + s / 2)
            g.drawLine(11 * s, 4 * s + s / 2, 11 * s + s / 2, 4 * s)
            g.drawLine(13 * s, 4 * s, 13 * s + s / 2, 4 * s + s / 2)
            g.drawLine(13 * s + s / 2, 4 * s + s / 2, 14 * s, 4 * s)
            g.color = PINK
            g.fillOval(9 * s, 5 * s, 2 * s, s)
            g.fillOval(13 * s + s / 2, 5 * s, 2 * s, s)
        } else {
            g.color = BLACK
            g.fillOval(10 * s + s / 2, 3 * s + s / 2, s + s / 2, s + s / 2)
            g.fillOval(13 * s, 3 * s + s / 2, s + s / 2, s + s / 2)
            g.color = WHITE
            g.fillOval(10 * s + s, 3 * s + s, s / 2, s / 2)
            g.fillOval(13 * s + s / 2, 3 * s + s, s / 2, s / 2)
        }
        g.color = PINK
        g.fillRect(12 * s, 5 * s, s, s)
        g.color = BLACK
        g.drawRect(12 * s, 5 * s, s, s)
    }

    private fun drawSleeping(g: Graphics2D, pal: Palette, cat: Cat) {
        val s = SCALE
        // sombra más grande dormido
        g.color = Color(0, 0, 0, 28)
        g.fillRoundRect(1 * s, 12 * s, 14 * s, 2 * s, 6, 6)
        // cuerpo hecho bolita - más tierno
        g.color = pal.base
        g.fillRoundRect(3 * s, 7 * s, 10 * s, 5 * s, s*2, s*2)
        // pancita
        g.color = if (cat.color == CatColor.WHITE) Color(0xF0, 0xF0, 0xF0) else WHITE
        g.fillRoundRect(5 * s, 9 * s, 6 * s, 2 * s, s, s)
        // cola enroscada adelante
        g.color = pal.base
        g.fillRoundRect(2 * s, 8 * s, 4 * s, 4 * s, s*2, s*2)
        g.color = pal.dark
        g.drawRoundRect(3 * s, 7 * s, 10 * s, 5 * s, s*2, s*2)
        g.drawRoundRect(2 * s, 8 * s, 4 * s, 4 * s, s*2, s*2)
        // cabeza acurrucada
        g.color = pal.base
        g.fillRoundRect(9 * s, 5 * s, 6 * s, 5 * s, s*2, s*2)
        // orejitas caídas (dormido)
        g.fillRect(9 * s, 4 * s, 2 * s, 2 * s)
        g.fillRect(13 * s, 4 * s, 2 * s, 2 * s)
        g.color = Color(0xFF, 0xC8, 0xD0)
        g.fillRect(9 * s + s/2, 4 * s + s/2, s, s)
        g.fillRect(13 * s + s/2, 4 * s + s/2, s, s)
        g.color = pal.dark
        g.drawRoundRect(9 * s, 5 * s, 6 * s, 5 * s, s*2, s*2)
        // ojos cerrados tiernos — — 
        g.color = BLACK
        g.drawLine(10 * s + s/2, 7 * s, 11 * s + s/2, 7 * s)
        g.drawLine(13 * s, 7 * s, 14 * s - s/2, 7 * s)
        // nariz rosita chiquita
        g.color = PINK
        g.fillRect(12 * s, 8 * s, s, s/2)
        // mejilla sonrojada dormido
        g.color = Color(0xFF, 0xB0, 0xC0, 120)
        g.fillOval(9 * s + s/2, 8 * s, s + s/2, s)
        // respiración sutil (frame)
        val breath = if (cat.walkFrame % 2 == 0) 0 else 1
        g.color = pal.dark
        g.drawArc(5 * s, 8 * s + breath, 6 * s, 2 * s, 0, 180)
    }

    private fun drawStretching(g: Graphics2D, pal: Palette, cat: Cat) {
        val s = SCALE
        g.color = Color(0, 0, 0, 30)
        g.fillRoundRect(0 * s, 12 * s, 15 * s, 2 * s, 4, 4)
        // cuerpo super estirado kawaii
        g.color = pal.base
        g.fillRoundRect(0 * s, 7 * s, 13 * s, 4 * s, s*2, s*2)
        // pancita estirada
        g.color = WHITE
        g.fillRoundRect(2 * s, 8 * s, 8 * s, 2 * s, s, s)
        // patitas delanteras bien adelante
        g.color = pal.base
        g.fillRect(13 * s, 6 * s, 2 * s, 3 * s)
        g.fillRect(13 * s, 9 * s, 3 * s, 1 * s) // garritas
        g.color = pal.dark
        g.drawRoundRect(0 * s, 7 * s, 13 * s, 4 * s, s*2, s*2)
        // cabeza estirada bajita
        g.color = pal.base
        g.fillRoundRect(10 * s, 5 * s, 6 * s, 4 * s, s*2, s*2)
        g.color = WHITE
        g.fillRoundRect(10 * s, 7 * s, 6 * s, 2 * s, s, s)
        g.color = pal.dark
        g.drawRoundRect(10 * s, 5 * s, 6 * s, 4 * s, s*2, s*2)
        // orejas
        g.color = pal.base
        g.fillRect(10 * s, 4 * s, 2 * s, 2 * s)
        g.fillRect(14 * s, 4 * s, 2 * s, 2 * s)
        g.color = PINK
        g.fillRect(10 * s + s/2, 4 * s + s/2, s, s)
        g.fillRect(14 * s + s/2, 4 * s + s/2, s, s)
        // ojitos entrecerrados de estiramiento >_<
        g.color = BLACK
        g.drawLine(11 * s, 6 * s + s/2, 12 * s, 6 * s)
        g.drawLine(12 * s, 6 * s, 12 * s + s/2, 6 * s + s/2)
        g.drawLine(13 * s + s/2, 6 * s + s/2, 14 * s, 6 * s)
        g.drawLine(14 * s, 6 * s, 15 * s, 6 * s + s/2)
        g.color = PINK
        g.fillOval(10 * s, 8 * s, s, s/2)
    }

    fun drawHeart(g: Graphics2D, x: Int, y: Int, scale: Float, alpha: Int) {
        val g2 = g.create() as Graphics2D
        try {
            g2.translate(x.toDouble(), y.toDouble())
            g2.scale(scale.toDouble(), scale.toDouble())
            g2.color = Color(255, 90, 120, alpha.coerceIn(0, 255))
            val p = arrayOf("0110110","1111111","1111111","0111110","0011100","0001000")
            for (row in p.indices) for (col in p[row].indices) if (p[row][col]=='1') g2.fillRect(col*2,row*2,2,2)
        } finally { g2.dispose() }
    }

    fun drawZzz(g: Graphics2D, x: Int, y: Int, alpha: Int) {
        val g2 = g.create() as Graphics2D
        try {
            g2.color = Color(100, 140, 255, alpha.coerceIn(0, 255))
            g2.font = g2.font.deriveFont(10f)
            g2.drawString("Z", x, y)
            g2.drawString("z", x+6, y+6)
            g2.drawString("z", x+2, y+12)
        } finally { g2.dispose() }
    }

    fun drawLaser(g: Graphics2D, x: Int, y: Int) {
        val g2 = g.create() as Graphics2D
        try {
            g2.color = Color(255, 30, 30, 90)
            g2.fillOval(x-8, y-8, 16, 16)
            g2.color = Color(255, 20, 20)
            g2.fillOval(x-4, y-4, 8, 8)
            g2.color = Color.WHITE
            g2.fillOval(x-1, y-1, 2, 2)
        } finally { g2.dispose() }
    }

    fun drawYarn(g: Graphics2D, x: Int, y: Int, rot: Float, life: Int) {
        val g2 = g.create() as Graphics2D
        try {
            val alpha = (200 + (life % 55)).coerceIn(120, 255)
            g2.color = Color(0, 0, 0, 30)
            g2.fillOval(x-10, y+8, 20, 6)
            g2.translate(x.toDouble(), y.toDouble())
            g2.rotate(Math.toRadians(rot.toDouble()))
            g2.color = Color(0xFF, 0x6B, 0x9D, alpha)
            g2.fillOval(-10, -10, 20, 20)
            g2.color = Color(0xFF, 0xD0, 0xE8, 180)
            g2.drawOval(-8, -8, 16, 16)
            g2.drawOval(-5, -5, 10, 10)
            g2.color = Color.WHITE
            g2.drawLine(-10, 0, 10, 0)
            g2.drawLine(0, -10, 0, 10)
            g2.color = Color(0xFF, 0x6B, 0x9D)
            g2.drawArc(8, -2, 12, 8, 0, 180)
        } finally { g2.dispose() }
    }

    private fun drawEars(g: Graphics2D, pal: Palette, cat: Cat) {
        val s = SCALE
        val pointy = cat.earType == EarType.POINTY
        if (pointy) {
            // triangulito
            val lx = intArrayOf(9*s, 10*s, 11*s)
            val ly = intArrayOf(3*s, 0*s, 3*s)
            val rx = intArrayOf(13*s, 14*s, 15*s)
            val ry = intArrayOf(3*s, 0*s, 3*s)
            g.color = pal.base
            g.fillPolygon(lx, ly, 3)
            g.fillPolygon(rx, ry, 3)
            g.color = PINK
            val lix = intArrayOf(9*s + s/2, 10*s, 10*s + s/2)
            val liy = intArrayOf(3*s - s/2, s, 3*s - s/2)
            val rix = intArrayOf(13*s + s/2, 14*s, 14*s + s/2)
            val riy = intArrayOf(3*s - s/2, s, 3*s - s/2)
            g.fillPolygon(lix, liy, 3)
            g.fillPolygon(rix, riy, 3)
            g.color = pal.dark
            g.drawPolygon(lx, ly, 3)
            g.drawPolygon(rx, ry, 3)
        } else {
            // redondeadas
            g.color = pal.base
            g.fillRoundRect(9*s, 1*s, 2*s, 2*s, s, s)
            g.fillRoundRect(13*s, 1*s, 2*s, 2*s, s, s)
            g.color = PINK
            g.fillOval(9*s + s/2, 1*s + s/2, s, s)
            g.fillOval(13*s + s/2, 1*s + s/2, s, s)
            g.color = pal.dark
            g.drawRoundRect(9*s, 1*s, 2*s, 2*s, s, s)
            g.drawRoundRect(13*s, 1*s, 2*s, 2*s, s, s)
        }
    }

    private fun drawHat(g: Graphics2D, cat: Cat) {
        val s = SCALE
        when (cat.hat) {
            HatType.NONE -> return
            HatType.SANTA -> {
                // gorro rojo con borde blanco
                g.color = Color(0xD8, 0x20, 0x20)
                val hx = intArrayOf(10*s, 12*s, 14*s)
                val hy = intArrayOf(2*s, -1*s, 2*s)
                g.fillPolygon(hx, hy, 3)
                g.color = Color.WHITE
                g.fillRect(9*s, 2*s, 6*s, s)
                g.fillOval(12*s - s/2, -1*s - s/2, s+1, s+1)
                g.color = Color(0xA0, 0x10, 0x10)
                g.drawPolygon(hx, hy, 3)
            }
            HatType.PARTY -> {
                g.color = Color(0xFF, 0xE0, 0x40)
                val hx = intArrayOf(11*s, 12*s, 13*s)
                val hy = intArrayOf(2*s, -1*s + s/2, 2*s)
                g.fillPolygon(hx, hy, 3)
                g.color = Color(0xFF, 0x60, 0x80)
                g.fillRect(11*s, 0*s, s, 2*s)
                g.fillRect(12*s + s/2, 0*s, s/2, 2*s)
                g.color = Color.WHITE
                g.fillOval(12*s -2, -1*s, 4, 4)
            }
            HatType.WITCH -> {
                g.color = Color(0x20, 0x20, 0x20)
                g.fillRect(9*s, 2*s, 6*s, s/2)
                val hx = intArrayOf(10*s, 12*s, 14*s)
                val hy = intArrayOf(2*s, -2*s, 2*s)
                g.fillPolygon(hx, hy, 3)
                g.color = Color(0xFF, 0xB0, 0x40)
                g.fillRect(11*s, 0*s, 2*s, s/2)
            }
            HatType.BEANIE -> {
                g.color = Color(0x4A, 0x90, 0xD9)
                g.fillRoundRect(9*s, 0*s, 6*s, 2*s + s/2, s, s)
                g.color = Color.WHITE
                g.fillOval(11*s, -1*s, 4, 4)
                g.color = Color(0xFF, 0xD0, 0x40)
                g.drawLine(10*s, 1*s, 14*s, 1*s)
            }
            HatType.BOW -> {
                g.color = PINK
                // moño entre orejas
                g.fillOval(11*s, 0*s + s/2, 2*s, s + s/2)
                g.fillOval(12*s + s/2, 0*s + s/2, 2*s, s + s/2)
                g.color = DARK_PINK
                g.fillOval(12*s, 0*s + s/2 + 2, s, s)
            }
        }
    }

    fun drawPaw(g: Graphics2D, x: Int, y: Int, alpha: Int) {
        val g2 = g.create() as Graphics2D
        try {
            g2.color = Color(0x5A, 0x3A, 0x2A, alpha.coerceIn(0, 90))
            // almohadilla central + 3 deditos
            g2.fillOval(x, y, 6, 5)
            g2.fillOval(x-1, y-3, 3, 3)
            g2.fillOval(x+2, y-4, 3, 3)
            g2.fillOval(x+5, y-3, 3, 3)
        } finally { g2.dispose() }
    }
}
