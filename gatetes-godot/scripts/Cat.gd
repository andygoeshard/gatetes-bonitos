extends CharacterBody2D

@export var cat_color: String = "orange" # orange, white, black, gray, calico, cream, brown, siamese, tabby, tuxedo
@export var speed: float = 80.0
@export var ear_type: String = "round" # pointy / round
@export var hat_type: String = "none" # none, santa, party, witch, beanie, bow

var dir := Vector2(1, 0.6).normalized()
var state := "walking" # walking, sitting, sleeping, stretching, dragging, chasing
var state_timer := 0.0
var walk_frame := 0
var frame_timer := 0.0
var dragging := false
var drag_offset := Vector2.ZERO
var chase_target: Vector2 = Vector2.ZERO
var has_chase := false

var palettes := {
	"orange": [Color("#FFB86C"), Color("#E88A30"), Color("#FFD8B0")],
	"white": [Color("#FEFEFE"), Color("#D8DDE2"), Color("#FFFFFF")],
	"black": [Color("#3A3A40"), Color("#1A1A1E"), Color("#6A6A70")],
	"gray": [Color("#B0B8C2"), Color("#7E8895"), Color("#E0E6ED")],
	"calico": [Color("#FFAB73"), Color("#D87A3A"), Color("#FFE0B8")],
	"cream": [Color("#FFF0C8"), Color("#E8C890"), Color("#FFFFE8")],
	"brown": [Color("#8B5A2B"), Color("#5A3515"), Color("#C88A60")],
	"siamese": [Color("#E8DDC8"), Color("#5A4535"), Color("#FFF0E0")],
	"tabby": [Color("#D8A060"), Color("#8B5A30"), Color("#FFD090")],
	"tuxedo": [Color("#2E2E32"), Color("#0E0E10"), Color("#FFFFFF")],
}

func _ready():
	randomize()
	dir = Vector2(randf_range(-1, 1), randf_range(-1, 1)).normalized()
	if dir.length() < 0.3:
		dir = Vector2(1, 0.6).normalized()
	state_timer = randf_range(3, 6)
	add_to_group("cats")
	# sprite via ColorRect placeholder + draw
	queue_redraw()

func _process(delta):
	frame_timer += delta
	if frame_timer > 0.18:
		frame_timer = 0
		walk_frame = (walk_frame + 1) % 4
		queue_redraw()
	if state == "sleeping" and randf() < 0.04:
		queue_redraw()

func _physics_process(delta):
	if dragging:
		return
	if state == "chasing" and has_chase:
		var to_target = chase_target - global_position
		if to_target.length() < 15:
			has_chase = false
			state = "sitting"
			state_timer = 2
			return
		dir = to_target.normalized()
		velocity = dir * 140
		move_and_slide()
		_bounce()
		return
	if state in ["sleeping", "sitting", "stretching"]:
		state_timer -= delta
		if state_timer <= 0:
			_pick_next_state()
		return
	# walking
	velocity = dir * speed
	move_and_slide()
	_bounce()
	state_timer -= delta
	if state_timer <= 0 or randf() < 0.015:
		_pick_next_state()
	# salto ocasional
	if randf() < 0.003:
		velocity.y -= 120

func _bounce():
	var vp = get_viewport_rect().size
	if global_position.x < 10:
		global_position.x = 10; dir.x = abs(dir.x)
	if global_position.x > vp.x - 50:
		global_position.x = vp.x - 50; dir.x = -abs(dir.x)
	if global_position.y < 10:
		global_position.y = 10; dir.y = abs(dir.y)
	if global_position.y > vp.y - 50:
		global_position.y = vp.y - 50; dir.y = -abs(dir.y)
	if randf() < 0.008:
		dir = dir.rotated(randf_range(-0.6, 0.6)).normalized()

func _pick_next_state():
	var r = randf()
	if r < 0.28:
		state = "sitting"; state_timer = randf_range(2, 4)
	elif r < 0.45:
		state = "sleeping"; state_timer = randf_range(4, 8)
	elif r < 0.55:
		state = "stretching"; state_timer = 1.2
	else:
		dir = Vector2(randf_range(-1,1), randf_range(-1,1)).normalized()
		state = "walking"; state_timer = randf_range(3, 6)
	queue_redraw()

func start_drag(mouse_pos: Vector2):
	dragging = true
	state = "dragging"
	drag_offset = global_position - mouse_pos

func drag_to(mouse_pos: Vector2):
	global_position = mouse_pos + drag_offset

func end_drag():
	dragging = false
	state = "walking"
	state_timer = randf_range(2, 4)
	dir = Vector2(randf_range(-1,1), randf_range(-1,1)).normalized()

func chase(pos: Vector2):
	has_chase = true
	chase_target = pos
	state = "chasing"
	state_timer = 4

func _draw():
	var s = 3
	var pal = palettes.get(cat_color, palettes["orange"])
	var base: Color = pal[0]
	var dark: Color = pal[1]
	# sombra
	draw_rect(Rect2(4, 38, 32, 6), Color(0,0,0,0.15))
	# cuerpo 9x5
	draw_rect(Rect2(6, 18, 27, 15), base)
	draw_rect(Rect2(6, 18, 27, 15), dark, false, 1)
	# pancita
	draw_rect(Rect2(12, 24, 15, 9), Color.WHITE if cat_color != "white" else Color("#E8E8E8"))
	# cabeza 6x5
	draw_rect(Rect2(27, 6, 18, 15), base)
	draw_rect(Rect2(27, 6, 18, 15), dark, false, 1)
	draw_rect(Rect2(27, 15, 18, 6), Color.WHITE)
	# orejas
	if ear_type == "pointy":
		var lx = PackedVector2Array([Vector2(27,9), Vector2(30,0), Vector2(33,9)])
		var rx = PackedVector2Array([Vector2(39,9), Vector2(42,0), Vector2(45,9)])
		draw_colored_polygon(lx, base)
		draw_colored_polygon(rx, base)
		draw_polyline(lx, dark, 1)
		draw_polyline(rx, dark, 1)
		draw_colored_polygon(PackedVector2Array([Vector2(29,8), Vector2(30,3), Vector2(31,8)]), Color("#FF8FAB"))
		draw_colored_polygon(PackedVector2Array([Vector2(41,8), Vector2(42,3), Vector2(43,8)]), Color("#FF8FAB"))
	else:
		draw_rect(Rect2(27, 3, 6, 6), base)
		draw_rect(Rect2(39, 3, 6, 6), base)
		draw_circle(Vector2(30,6), 1.5, Color("#FF8FAB"))
		draw_circle(Vector2(42,6), 1.5, Color("#FF8FAB"))
	# ojos
	if state == "sleeping":
		draw_line(Vector2(31,20), Vector2(34,20), Color.BLACK, 1)
		draw_line(Vector2(39,20), Vector2(42,20), Color.BLACK, 1)
	else:
		draw_circle(Vector2(32,14), 2, Color.BLACK)
		draw_circle(Vector2(40,14), 2, Color.BLACK)
		draw_circle(Vector2(33,13), 0.8, Color.WHITE)
		draw_circle(Vector2(41,13), 0.8, Color.WHITE)
	# nariz
	draw_rect(Rect2(36, 17, 3, 2), Color("#FF8FAB"))
	# gorrito
	if hat_type != "none":
		match hat_type:
			"santa":
				draw_colored_polygon(PackedVector2Array([Vector2(30,6), Vector2(36,-4), Vector2(42,6)]), Color("#D82020"))
				draw_rect(Rect2(27,6,18,3), Color.WHITE)
				draw_circle(Vector2(36,-4), 2, Color.WHITE)
			"party":
				draw_colored_polygon(PackedVector2Array([Vector2(33,6), Vector2(36,-2), Vector2(39,6)]), Color("#FFE040"))
				draw_circle(Vector2(36,-2), 2, Color.WHITE)
			"witch":
				draw_rect(Rect2(27,6,18,1), Color.BLACK)
				draw_colored_polygon(PackedVector2Array([Vector2(30,6), Vector2(36,-6), Vector2(42,6)]), Color.BLACK)
			"beanie":
				draw_rect(Rect2(27,0,18,7), Color("#4A90D9"), true)
				draw_circle(Vector2(36,-1), 2, Color.WHITE)
			"bow":
				draw_circle(Vector2(33,2), 3, Color("#FF8FAB"))
				draw_circle(Vector2(39,2), 3, Color("#FF8FAB"))
				draw_circle(Vector2(36,3), 2, Color("#E05A7A"))
