# GDExtension stub para macOS fullscreen overlay
# Si compilas la extensión en Swift/ObjC, registra singleton "MacWindowLevel" con método make_overlay(window_id: int)
# Sin la extensión, el juego igual funciona en ventana; solo no se verá sobre fullscreen.
# Ejemplo Swift (macos_window_level.swift):
#
# import Cocoa
# @objc public class MacWindowLevel: NSObject {
#   @objc public func make_overlay(_ windowId: Int64) {
#     DispatchQueue.main.async {
#       if let window = NSApp.window(withWindowNumber: Int(windowId)) {
#         window.level = .screenSaver
#         window.collectionBehavior = [.canJoinAllSpaces, .stationary, .fullScreenAuxiliary]
#         window.isOpaque = false
#         window.backgroundColor = .clear
#         window.hasShadow = false
#         window.ignoresMouseEvents = false
#       }
#     }
#   }
# }
#
# Compila como .dylib y declara en gatetes.gdextension:
# [configuration]
# entry_symbol = "gatetes_library_init"
# compatibility_minimum = 4.3
# [libraries]
# macos.debug.arm64 = "res://bin/libgatetes.macos.debug.arm64.dylib"

extends RefCounted
