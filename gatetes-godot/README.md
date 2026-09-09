# Gatetes Desktop — Godot para Mac (overlay sobre fullscreen)

Ventana transparente **always-on-top** con 10 gatitos pixel art que caminan por **toda la pantalla**, incluso sobre apps en fullscreen.

## Abrir en Godot 4.3+

```bash
cd gatetes-godot
# Abre Godot → Import → selecciona project.godot → Run (F5)
```

Controles:
- Click corto gato → caricia
- Arrastrar gato/pelotita → física al soltar
- Shift/Alt+Click → láser
- Doble click vacío → pelotita

## Exportar a .app Mac

1. Godot → Project → Export → Add → macOS
2. Options: `window/transparent = true`, `window/always_on_top = true`
3. Export → `Gatetes Desktop.app`

### Fix fullscreen (importante)

macOS fullscreen crea un Space aparte y taparía la ventana. Para que se vea encima:

- Opción A (sin código): deja el overlay en un Space normal y no uses fullscreen nativo, usa ventana maximizada
- Opción B (nativo): compila `scripts/MacWindowLevel.gd` como GDExtension Swift (ver comentarios en el archivo) que pone `window.level = .screenSaver` + `canJoinAllSpaces`. Sin esto, en fullscreen no se ve.

Archivo stub: `scripts/MacWindowLevel.gd` — ya está documentado con el Swift listo para compilar como `bin/libgatetes.macos.*.dylib`
