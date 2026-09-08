# 😺 Gatetes Bonitos

<p align="center">
  <img src="src/main/resources/META-INF/pluginIcon.svg" width="100" alt="Gatetes"/>
  <br/>
  <em>Gatitos pixel art que viven en tu IDE. ¡Acaricialos, arrástralos y juega con ellos!</em>
</p>

<p align="center">
  <a href="https://github.com/andygoeshard/gatetes-bonitos"><img src="https://img.shields.io/badge/GitHub-gatetes--bonitos-24292e?logo=github" alt="GitHub"/></a>
  <img src="https://img.shields.io/badge/Platform-IntelliJ%20%7C%20Android%20Studio-087CFA?logo=intellijidea" alt="Platform"/>
  <img src="https://img.shields.io/badge/Kotlin-2.3.20-7F52FF?logo=kotlin" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/IDE-2025.3%20—%202026.1-00D8FF" alt="IDE"/>
  <img src="https://img.shields.io/badge/cats-10%20únicos-FFB86C" alt="Cats"/>
</p>

<p align="center">
  <strong>10 gatitos únicos • 2D libre por toda la pantalla • Gorritos • Huellitas • Pelotita con física • Láser • Sidebar</strong>
</p>

---

## ✨ Demo

> ![demo](https://via.placeholder.com/800x400/FFE4B5/8B4513?text=😸+Gatetes+Bonitos+-+agrega+tu+GIF+aquí)

---

## 🐾 Features

| Feature | Descripción |
|---------|-------------|
| **10 michis únicos** | Naranja · Blanco · Negro · Gris · Calicó · Crema · Marrón · Siamés · Atigrado · Smoking. Cada uno con paleta y patrón propio (rayas, manchas, seal-point, smoking). |
| **Orejas** | 5 con triangulito ▲ (Siamés/Tabby/Negro/Tuxedo/Marrón) y 5 redondeadas ◠ — se distinguen al toque |
| **Movimiento 2D** | `vx`+`vy` libres, rebote en 4 paredes, salto con gravedad, persecución |
| **Animaciones** | Caminar (4 frames) · Sentado · Durmiendo (bolita + Zzz) · Estirando · Asustado (¡pelos parados!) · Celebrando panza arriba |
| **Caricias** | Click corto → `♥` corazones + ojos `^ ^` · Drag → arrastrar y soltar con impulso |
| **Láser** | `Shift+Click` o `Alt+Click` en cualquier lado → punto rojo, todos persiguen |
| **Pelotita** | `🧶` con física (gravedad, fricción, rebote, rotación). **¡Arrastrable!** Arrastrá y soltá con impulso, los gatitos la cazan |
| **Huellitas** | Toggle `🐾` — dejan `🐾` 6s donde pisan y se desvanecen |
| **Gorritos** | Toggle `🎩` — 45% spawnean con Santa / Party / Witch / Beanie / Bow |
| **Reacción a builds** | `✅ Build OK` → celebran · `❌ Build falló` → se asustan y huyen. Hook a Gradle listo |
| **Sidebar** | Tool Window `Gatetes` (barra derecha) con controles completos sin tocar el menú |
| **Menú** | `Tools > Gatitos` — toggle por color (✓), cantidad 1-10, juegos, extras |

---

## 🎮 Uso

### Atajos
- **Click corto** en gato → acariciar (`♥`)
- **Arrastrar gato** → mover + soltar con impulso
- **Arrastrar pelotita** → mover + soltar con física
- **Shift+Click / Alt+Click** en vacío → láser rojo
- **Doble click** en vacío → láser

### Menú `Tools > Gatitos`
```
Gatitos
├── Mostrar/Ocultar Gatitos
├── Gatitos por color (10 únicos)  ✓ toggle por color
├── Cantidad (1-10)
├── Juegos
│   ├── 🧶 Lanzar pelotita
│   ├── ✅ Simular build OK
│   └── ❌ Simular build falló
└── Extras
    ├── 🎩 Gorritos ON/OFF
    └── 🐾 Huellitas ON/OFF
```

### Sidebar `Gatetes` (barra derecha)
Panel con todo lo anterior + estado en vivo, sin necesidad de menú. Botones 1-10, toggles de color, hats/paws, juegos y tips.

---

## 🛠️ Instalación (desarrollo)

```bash
git clone https://github.com/andygoeshard/gatetes-bonitos.git
cd gatetes-bonitos

# Correr IDE sandbox con el plugin
./gradlew runIde

# Build distribución
./gradlew buildPlugin
# → build/distributions/gatetes-bonitos-1.0.0-SNAPSHOT.zip

# Instalar en tu Android Studio / IntelliJ
# Settings > Plugins > ⚙️ > Install Plugin from Disk > gatetes-bonitos-*.zip
```

**Requisitos:** JDK 17+, IntelliJ 2025.3.5 (253) — compatible con Android Studio 2026.1 (261) `since-build 253`

---

## 🧩 Estructura del proyecto

```
.
├── src/main/kotlin/com/gatetes/
│   ├── cat/
│   │   ├── CatModel.kt              # Cat, CatColor(10), HatType, EarType, estados
│   │   ├── CatRenderer.kt           # Pixel art 16×16×3, paletas, orejas triangulito, gorritos
│   │   ├── CatOverlayPanel.kt       # Overlay transparente, timer 30fps, drag, láser, pelotita, huellitas
│   │   ├── CatManager.kt            # Singleton, persistencia PropertiesComponent, hasta 10
│   │   ├── YarnBall.kt              # Física pelotita
│   │   └── CatBuildListener.kt      # Hook builds
│   └── GatetesToolWindowFactory.kt  # Sidebar
├── src/main/resources/META-INF/
│   ├── plugin.xml
│   └── pluginIcon.svg
└── build.gradle.kts
```

---

## 🗺️ Roadmap

- [x] 10 gatitos únicos con patrones
- [x] Orejas triangulito / redondeadas
- [x] Gorritos toggleables
- [x] Huellitas toggleables
- [x] Pelotita arrastrable con física
- [x] Sidebar completa
- [ ] Caja de cartón donde se esconden
- [ ] Soniditos `miau` (toggle)
- [ ] Nombres y collares personalizables
- [ ] Publicar en Marketplace (cuando quieras)

---

## 💖 Créditos

Hecho con mucho amor gatuno por [@andygoeshard](https://github.com/andygoeshard) + `Muse Spark`.

> ¿Ideas? ¡Abre un issue o PR! Toda PR con un gatito nuevo es bienvenida 😸

<p align="center">
  <sub>Made with 🐾 in Argentina</sub>
</p>
