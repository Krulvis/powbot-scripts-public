package org.powbot.krulvis.api.extensions

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Varpbits
import org.powbot.krulvis.api.ATContext.me

object Cannon {

	const val BASE = 6
	const val STAND = 8
	const val BARRELS = 10
	const val FURNACE = 12
	val items = intArrayOf(BASE, STAND, BARRELS, FURNACE)

	private const val STATE = 2
	private const val BALLS = 3
	private const val COORDS = 4
	fun balls() = Varpbits.varpbit(BALLS)
	fun state() = Varpbits.varpbit(STATE)
	fun firing() = Varpbits.varpbit(1) != 0

	fun placed() = state() == 4
	fun tile(): Tile {
		val c = Varpbits.varpbit(COORDS)
		val x = (c.shr(14) and 0x3FFF) + 1
		val y = (c and 0x3FFF) + 1
		val z = c.shr(28) and 0x3
		return Tile(x, y, z)
	}

	fun getCannon() =
		Objects.stream(tile(), GameObject.Type.INTERACTIVE).name("Broken multicannon", "Dwarf multicannon").first()

	fun standingTiles(monsterSize: Int = 2): List<Tile> {
		val tile = tile()
		val tiles = if (monsterSize in 3..4) {
			listOf(tile.derive(1, 1), tile.derive(-1, -1), tile.derive(-1, 1), tile.derive(1, -1))
		} else if (monsterSize == 2) {
			listOf(tile.derive(-2, -2), tile.derive(-2, 3), tile.derive(3, 3), tile.derive(3, -2))
		} else {
			listOf(me.tile())
		}
		return tiles.filter { it.reachable() }
	}
}