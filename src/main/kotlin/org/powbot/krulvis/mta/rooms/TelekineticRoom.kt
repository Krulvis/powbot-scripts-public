package org.powbot.krulvis.mta.rooms


import org.powbot.api.Area
import org.powbot.api.Color
import org.powbot.api.Color.CYAN
import org.powbot.api.Color.GREEN
import org.powbot.api.Rectangle
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.Flag
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.tree.branches.FinishedMaze
import org.powbot.mobile.drawing.Rendering
import org.powbot.util.TransientGetter2D
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object TelekineticRoom : MTARoom {
	val logger = LoggerFactory.getLogger(javaClass.simpleName)!!


	override val portalName: String = "Telekinetic"
	override fun rootComponent(mta: MTA): TreeComponent<MTA> {
		return FinishedMaze(mta)
	}

	override val WIDGET_ID = 198
	const val MAZE_GUARDIAN_MOVING: Int = 6778
	private val TELEKINETIC_WALL: Int = 10755
	private val TELEKINETIC_FINISH: Int = 23672

	private var telekineticWalls: List<GameObject> = emptyList()

	private var moves: Stack<Pair<Tile, Direction>> = Stack<Pair<Tile, Direction>>()
	private var guardianDestination: Tile = Tile.Nil
	var finishLocation: Tile = Tile.Nil
	private var bounds: Rectangle = Rectangle(-1, -1, -1, -1)
	private var numMazeWalls = 0
	var mazeGuardian: Npc = Npc.Nil
	var currentMove: Pair<Tile, Direction> = Tile.Nil to Direction.NORTH
	var nextOptimal = Tile.Nil

	fun resetRoom() {
		finishLocation = Tile.Nil
		telekineticWalls = emptyList()
	}

	fun shouldInstantiate() = finishLocation == Tile.Nil

	fun instantiateRoom() {
		logger.info("Getting finish location")
		getFinish()
		logger.info("Getting walls")
		getWalls()
		logger.info("Calculating bounds")
		getBounds(telekineticWalls)
		buildMoves()
	}

	fun withinBounds() = bounds.contains(me.tile())

	fun getFinish() {
		finishLocation = Objects.stream().type(GameObject.Type.FLOOR_DECORATION).id(TELEKINETIC_FINISH).first().tile
		logger.info("FinishLocation=$finishLocation")
	}

	fun getWalls() {
		telekineticWalls = Objects.stream().type(GameObject.Type.BOUNDARY).id(TELEKINETIC_WALL).toList()
		numMazeWalls = telekineticWalls.size
		logger.info("Walls=$numMazeWalls")
	}

	fun nearestWall(): GameObject =
		if (telekineticWalls.isNotEmpty()) telekineticWalls.minByOrNull { it.distance() } ?: GameObject.Nil
		else Objects.stream().type(GameObject.Type.BOUNDARY).id(TELEKINETIC_WALL).nearest().first()

	fun getGuardian(flags: TransientGetter2D<Int>): Npc {
		mazeGuardian = Npcs.stream().name("", "Maze Guardian").first()
		guardianDestination(flags)
		return mazeGuardian
	}


	fun buildMoves() {
		val flags = getFlags()
		getGuardian(flags)
		logger.info("Calculating moves")
		build(flags)
		logger.info("Done building moves")
		getMove()
	}

	fun getFlags(): TransientGetter2D<Int> = Movement.collisionMap(0).flags()

	fun paint(g: Rendering) {
		if (inside() && numMazeWalls > 0) {
			guardianDestination.drawOnScreen(outlineColor = Color.WHITE)
			finishLocation.drawOnScreen(outlineColor = Color.WHITE)
			nextOptimal.drawOnScreen(outlineColor = CYAN)
			moves.forEachIndexed { i, move ->
				if (move.first != Tile.Nil) {
					val moveColor = if (move.second == standingDirection()) {
						GREEN
					} else {
						Color.RED
					}
					move.first.drawOnScreen("[$i]: ${move.second}", outlineColor = moveColor)
				}
			}
		}
	}


	fun optimal(): Tile {
		nextOptimal = optimalTileForDirection(currentMove.second)
		return nextOptimal
	}

	fun optimalTileForDirection(direction: Direction): Tile {
		val current: Tile = me.tile()
		val areaNext = getIndicatorLine(direction)
		return nearest(areaNext, current)
	}

	private fun nearest(area: Area, tile: Tile): Tile {
		var dist = Int.MAX_VALUE
		var nearest: Tile = Tile.Nil

		for (areaPoint in area.tiles) {
			val currDist = manhattan(areaPoint, tile)
			if (dist > currDist) {
				nearest = areaPoint
				dist = currDist
			}
		}

		return nearest
	}


	private fun build(flags: TransientGetter2D<Int>) {
		moves = buildEdges(guardianDestination, flags)
	}

	private fun guardianDestination(flags: TransientGetter2D<Int>) {
		guardianDestination = if (mazeGuardian.id == MAZE_GUARDIAN_MOVING) {
			val direction = Direction.fromGuardian()
			neighbour(mazeGuardian.tile(), direction, flags)
		} else {
			mazeGuardian.tile()
		}
	}

	private fun buildEdges(start: Tile, flags: TransientGetter2D<Int>): Stack<Pair<Tile, Direction>> {
		val visit: Queue<Tile> = LinkedList()
		val closed: MutableSet<Tile> = HashSet<Tile>()
		val scores: MutableMap<Tile, Int> = HashMap<Tile, Int>()
		val edges: MutableMap<Tile, Tile> = HashMap<Tile, Tile>()
		scores[start] = 0
		visit.add(start)
		var visitedN = 0

		while (!visit.isEmpty() && visitedN < 10000) {
			val next = visit.poll()
			closed.add(next)
			val neighbours = neighbours(next, flags)

			for (neighbour in neighbours) {
				if (neighbour != next && !closed.contains(neighbour)) {
					val score = scores[next]!! + 1

					if (!scores.containsKey(neighbour) || scores[neighbour]!! > score) {
						scores[neighbour] = score
						edges[neighbour] = next
						visit.add(neighbour)
					}
				}
			}
			visitedN++
		}
		logger.info("Done building edges visitedN=$visitedN")

		return buildMoves(edges, finishLocation)
	}

	private fun buildMoves(edges: Map<Tile, Tile>, finish: Tile): Stack<Pair<Tile, Direction>> {
		val path: Stack<Pair<Tile, Direction>> = Stack<Pair<Tile, Direction>>()
		var current = finish

		while (edges.containsKey(current)) {
			val next = edges[current]!!

			if (next.x > current.x) {
				path.add(next to Direction.WEST)
			} else if (next.x < current.x) {
				path.add(next to Direction.EAST)
			} else if (next.y > current.y) {
				path.add(next to Direction.SOUTH)
			} else {
				path.add(next to Direction.NORTH)
			}

			current = next
		}

		return path
	}

	private fun neighbours(tile: Tile, flags: TransientGetter2D<Int>): Array<Tile> {
		return arrayOf(neighbour(tile, Direction.NORTH, flags), neighbour(tile, Direction.EAST, flags), neighbour(tile, Direction.SOUTH, flags), neighbour(tile, Direction.WEST, flags))
	}

	private fun neighbour(tile: Tile, direction: Direction, flags: TransientGetter2D<Int>): Tile {
		val dx: Int
		val dy: Int

		when (direction) {
			Direction.NORTH -> {
				dx = 0
				dy = 1
			}

			Direction.SOUTH -> {
				dx = 0
				dy = -1
			}

			Direction.EAST -> {
				dx = 1
				dy = 0
			}

			Direction.WEST -> {
				dx = -1
				dy = 0
			}
		}

		var travelTile = tile
		while (!travelTile.blocked(flags, direction.blockFlag) && bounds.contains(travelTile)) {
			travelTile = travelTile.derive(dx, dy)
		}
		return travelTile
	}

	private fun Rectangle.contains(tile: Tile) = contains(tile.x, tile.y)

	private fun getBounds(walls: List<GameObject>) {
		var minX = Int.MAX_VALUE
		var minY = Int.MAX_VALUE

		var maxX = Int.MIN_VALUE
		var maxY = Int.MIN_VALUE

		for (wall in walls) {
			val point: Tile = wall.tile
			minX = min(minX, point.x)
			minY = min(minY, point.y)

			maxX = max(maxX, point.x)
			maxY = max(maxY, point.y)
		}

		bounds = Rectangle(minX, minY, maxX - minX, maxY - minY)
		logger.info("Bounds = $bounds")
	}

	fun standingDirection(): Direction? {
		val mine = me.tile()
		if (mine.y >= bounds.y + bounds.height && mine.x < bounds.x + bounds.width && mine.x > bounds.x) {
			return Direction.NORTH
		} else if (mine.y <= bounds.y && mine.x < bounds.x + bounds.width && mine.x > bounds.x) {
			return Direction.SOUTH
		} else if (mine.x >= bounds.x + bounds.width && mine.y < bounds.y + bounds.height && mine.y > bounds.y) {
			return Direction.EAST
		} else if (mine.x <= bounds.x && mine.y < bounds.y + bounds.height && mine.y > bounds.y) {
			return Direction.WEST
		}
		return null
	}

	private fun getMove(): Pair<Tile, Direction> {
		currentMove = moves.firstOrNull { it.first == guardianDestination } ?: (Tile.Nil to Direction.NORTH)
		val index = moves.indexOf(currentMove)
		if (index in 0 until moves.size - 1) {
			repeat(moves.size - index - 1) { moves.removeLast() }
		}
		return currentMove
	}

	fun getNextMove(): Pair<Tile, Direction> {
		val currentIndex = moves.indexOf(currentMove)
		return if (currentIndex > 0) {
			moves[currentIndex - 1]
		} else {
			Tile.Nil to Direction.NORTH
		}
	}

	fun requiredStandingDirection(): Direction {
		return getMove().second
	}

	fun Tile.walk(): Boolean {
		if (this == Tile.Nil) return false
		val matrix = matrix()
		if (!matrix.inViewport() || matrix.distance() > 10) {
			return Movement.step(this)
		} else {
			matrix.click()
			if (Utils.waitFor(1000) { Movement.destination() == this }) {
				return Utils.waitForDistance(this) { this == me.tile() }
			}
		}
		return false
	}


	enum class Direction(val blockFlag: Int, vararg val orientations: Int) {
		NORTH(Flag.W_N, 4), EAST(Flag.W_E, 6), SOUTH(Flag.W_S, 0), WEST(Flag.W_W, 2), ;

		companion object {

			fun fromGuardian(): Direction {
				var dir: Direction?
				do {
					dir = fromOrientation(mazeGuardian.orientation())
				} while (dir == null)
				return dir
			}

			fun fromOrientation(orientation: Int): Direction? {
				return values().firstOrNull { orientation in it.orientations }
			}
		}
	}


	private fun boundedArea(x: Int, y: Int, width: Int, height: Int) = Area(Tile(x, y), Tile(x + width, y + height))

	private fun getIndicatorLine(direction: Direction): Area {
		return when (direction) {
			Direction.NORTH -> boundedArea(bounds.x + 1, bounds.y + bounds.height, bounds.width - 1, 1)
			Direction.SOUTH -> boundedArea(bounds.x + 1, bounds.y, bounds.width - 1, 1)
			Direction.WEST -> boundedArea(bounds.x, bounds.y + 1, 1, bounds.height - 1)
			Direction.EAST -> boundedArea(bounds.x + bounds.width, bounds.y + 1, 1, bounds.height - 1)
		}
	}


	private fun manhattan(point1: Tile, point2: Tile): Int {
		return abs(point1.x - point2.x) + abs(point2.y - point1.y)
	}
}