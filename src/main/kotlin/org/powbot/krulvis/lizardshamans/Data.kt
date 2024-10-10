package org.powbot.krulvis.lizardshamans

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.Npc
import org.powbot.krulvis.api.extensions.items.Item.Companion.RARE_DROP_TABLE
import org.powbot.util.TransientGetter2D
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt



object Data {

	val logger = LoggerFactory.getLogger(javaClass.simpleName)

	val LOOT = arrayOf(
		"Dragon warhammer",
		"Rune med helm",
		"Earth battlestaff",
		"Mystic earth staff",
		"Rune warhammer",
		"Rune chainbody",
		"Red d'hide vambraces",
		"Chaos rune",
		"Death rune",
		"Coal",
		"Iron ore",
		"Runite ore",
		"Grimy kwuarm",
		"Grimy cadantine",
		"Grimy dwarf weed",
		"Grimy lantadyme",
		"Ranarr seed",
		"Snapdragon seed",
		"Torstol seed",
		"Teak seed",
		"Yew seed",
		"Magic seed",
		"Spirit seed",
		"Dragonfruit tree seed",
		"Celastrus seed",
		"Redwood tree seed",
		"Lizardman fang",
		"Xeric's talisman",
		"Clue scroll (hard)",
		"Clue scroll (elite)",
		"Brimstone key",
		"Long bone",
		"Curved bone",
		*RARE_DROP_TABLE
	)

	enum class Direction(val x: Int, val y: Int) {
		NE(1, 1),
		NW(-1, 1),
		SE(1, -1),
		SW(-1, -1),
		;

		fun deriveTiles(tile: Tile) = listOf(tile.derive(x, 0), tile.derive(0, y))
	}

	fun furthestReachableTiles(centerTile: Tile, collisionMap: TransientGetter2D<Int>): List<Tile> {
		val directions = Direction.values()  // NW, NE, SW, SE
		val furthestTiles = MutableList(4) { Pair(centerTile, 0.0) }  // To store the furthest tiles and distances for NW, NE, SW, SE

		val queues = List(4) { LinkedList<Pair<Tile, Double>>() }  // (tile, distance, directionIndex)
		val visited = List(4) { mutableSetOf<Tile>() }
		for (i in directions.indices) {
			queues[i].add(Pair(centerTile, 0.0))
			visited[i].add(centerTile)
		}

		var whileIndex = 0
		while (queues.any { it.isNotEmpty() } && whileIndex < 1000) {
			whileIndex++
			directions.forEachIndexed { i, direction ->
				val queue = queues[i]
				if (queue.isNotEmpty()) {
					val (currentTile, distance) = queue.poll()
					val nextTiles = direction.deriveTiles(currentTile)

					nextTiles.forEach { nextTile ->
						if (!visited[i].contains(nextTile) && !nextTile.blocked(collisionMap) && nextTile.loaded() && nextTile.valid()) {
							val newDistance = nextTile.distance()
							visited[i].add(nextTile)
							queues[i].add(Pair(nextTile, newDistance))

							// Update the furthest tile for the current direction
							if (newDistance > furthestTiles[i].second) {
								furthestTiles[i] = Pair(nextTile, newDistance)
								if (newDistance > 15) {
									//Stop searching for this queue
									queues[i].clear()
								}
							}
						}
					}
				}
			}
		}

		return furthestTiles.map { it.first }
	}

	fun averagePerpendicularDistance(npcs: List<Npc>, lineA: Double, lineB: Double, lineC: Double): Double {
		return npcs.sumOf { perpendicularDistance(it.tile(), lineA, lineB, lineC) } / npcs.size
	}

	fun perpendicularDistance(tile: Tile, lineA: Double, lineB: Double, lineC: Double): Double {
		try {
			return abs(lineA * tile.x + lineB * tile.y + lineC) / sqrt(lineA * lineA + lineB * lineB)
		} catch (e: Exception) {
			logger.info("Got exception calculating perpendicularDistance")
			logger.error("Huh", e)
		}
		return -1.0
	}

	fun lineEquation(tile1: Tile, tile2: Tile): Triple<Double, Double, Double> {
		val a = tile2.y - tile1.y
		val b = tile1.x - tile2.x
		val c = tile2.x * tile1.y - tile1.x * tile2.y
		return Triple(a.toDouble(), b.toDouble(), c.toDouble())
	}

	enum class AttackAnimation(val animation: Int) {
		Falling(6946),
		Jump(7152),
		Summon(7157),
		;

		companion object {
			fun forAnimation(animation: Int) = values().firstOrNull { it.animation == animation }
		}
	}

	val SHAMAN_AREAS = listOf(
		Area(Tile(1283, 9963), Tile(1297, 9949)),
		Area(Tile(1298, 9955), Tile(1313, 9942)),
		Area(Tile(1315, 9958), Tile(1329, 9944)),
		Area(Tile(1321, 9974), Tile(1335, 9960))
	)
	val SLAYER_CAVE = Tile(1308, 9963, 0)
	val SLAYER_CAVE_ENTRANCE = Tile(1309, 3574, 0)
}