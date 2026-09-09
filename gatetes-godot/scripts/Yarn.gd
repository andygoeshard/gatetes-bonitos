extends Node2D

var vel := Vector2(randf_range(-120,120), randf_range(-120,120))
var rot_speed := randf_range(-200, 200)
var dragging := false
var drag_vel := Vector2.ZERO
var last_pos := Vector2.ZERO

func _ready():
	last_pos = global_position

func _process(delta):
	if dragging:
		return
	position += vel * delta
	vel += Vector2(0, 280) * delta
	vel *= 0.988
	rotation_degrees += rot_speed * delta
	rot_speed *= 0.99
	var vp = get_viewport_rect().size
	if position.x < 10:
		position.x = 10; vel.x = -vel.x * 0.78; rot_speed = -rot_speed
	if position.x > vp.x - 10:
		position.x = vp.x - 10; vel.x = -vel.x * 0.78; rot_speed = -rot_speed
	if position.y < 10:
		position.y = 10; vel.y = -vel.y * 0.72
	if position.y > vp.y - 16:
		position.y = vp.y - 16; vel.y = -vel.y * 0.62; vel.x *= 0.88
		if vel.length() < 40:
			vel.y = 0
	queue_redraw()

func drag_to(pos: Vector2):
	var delta = pos - global_position
	drag_vel = delta * 18
	global_position = pos
	last_pos = pos

func end_drag(pos: Vector2):
	dragging = false
	vel = drag_vel
	rot_speed = drag_vel.x * 1.2
	if vel.length() < 30:
		vel = Vector2(randf_range(-80,80), randf_range(-120,-40))

func _draw():
	# sombra
	draw_ellipse(Vector2(0, 10), Vector2(10, 3), Color(0,0,0,0.18))
	# pelotita
	draw_circle(Vector2.ZERO, 10, Color("#FF6B9D"))
	draw_arc(Vector2.ZERO, 8, 0, TAU, 16, Color("#FFD0E8"), 1)
	draw_arc(Vector2.ZERO, 5, 0, TAU, 10, Color("#FFD0E8"), 1)
	draw_line(Vector2(-10,0), Vector2(10,0), Color.WHITE, 1)
	draw_line(Vector2(0,-10), Vector2(0,10), Color.WHITE, 1)
	# hebra
	draw_arc(Vector2(8,-2), 6, 0, PI, 8, Color("#FF6B9D"), 1)
