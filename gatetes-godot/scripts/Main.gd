extends Node2D

@onready var cats_container := $Cats
var yarn_scene: PackedScene
var yarn: Node2D = null

var cat_colors = ["orange","white","black","gray","calico","cream","brown","siamese","tabby","tuxedo"]
var hats = ["none","santa","party","witch","beanie","bow"]

func _ready():
	# ventana transparente + siempre encima
	var win = get_window()
	win.transparent_bg = true
	win.borderless = true
	win.always_on_top = true
	# tamaño pantalla
	var screen_size = DisplayServer.screen_get_size()
	win.size = screen_size
	win.position = Vector2.ZERO
	# intentar fullscreen overlay fix macOS (ver MacWindowLevel.gd)
	_apply_macos_fullscreen_fix()
	spawn_cats(10)

func _apply_macos_fullscreen_fix():
	# Hook nativo: si existe GDExtension, lo aplica. Si no, no rompe.
	if Engine.has_singleton("MacWindowLevel"):
		var singleton = Engine.get_singleton("MacWindowLevel")
		if singleton and singleton.has_method("make_overlay"):
			singleton.make_overlay(get_window().get_window_id())

func spawn_cats(n: int):
	for i in range(n):
		var cat = preload("res://scripts/Cat.gd").new()
		# usamos CharacterBody2D via script, creamos nodo
		var node = CharacterBody2D.new()
		node.set_script(preload("res://scripts/Cat.gd"))
		node.cat_color = cat_colors[i % cat_colors.size()]
		node.hat_type = hats.pick_random() if randf() < 0.45 else "none"
		node.ear_type = "pointy" if cat_colors[i] in ["black","siamese","tabby","tuxedo","brown"] else "round"
		node.position = Vector2(randf_range(50, 1800), randf_range(100, 900))
		node.name = "Cat%d" % i
		cats_container.add_child(node)

func _input(event):
	if event is InputEventMouseButton and event.pressed:
		if event.shift_pressed or event.alt_pressed:
			# láser
			for c in cats_container.get_children():
				c.chase(get_global_mouse_position())
		elif event.button_index == MOUSE_BUTTON_LEFT:
			# check yarn primero
			if yarn and (yarn.global_position - get_global_mouse_position()).length() < 18:
				yarn.dragging = true
				return
			# check gato
			for c in cats_container.get_children():
				if (c.global_position - get_global_mouse_position()).length() < 30:
					c.start_drag(get_global_mouse_position())
					move_child(c, cats_container.get_child_count()-1) # al frente
					return
			# doble click pelotita
			if event.double_click:
				spawn_yarn(get_global_mouse_position())
	if event is InputEventMouseButton and not event.pressed:
		for c in cats_container.get_children():
			if c.dragging:
				c.end_drag()
		if yarn and yarn.dragging:
			yarn.end_drag(event.position)
	if event is InputEventMouseMotion:
		for c in cats_container.get_children():
			if c.dragging:
				c.drag_to(get_global_mouse_position())
		if yarn and yarn.dragging:
			yarn.drag_to(get_global_mouse_position())

func spawn_yarn(pos: Vector2 = Vector2(-1,-1)):
	if pos.x < 0:
		pos = get_viewport_rect().size / 2
	if yarn:
		yarn.queue_free()
	var y = preload("res://scripts/Yarn.gd").new()
	var node = Node2D.new()
	node.set_script(preload("res://scripts/Yarn.gd"))
	node.position = pos
	add_child(node)
	yarn = node
	for c in cats_container.get_children():
		c.chase(pos)

func _on_yarn_button_pressed():
	spawn_yarn()

func move_child(node, idx):
	cats_container.move_child(node, idx)
