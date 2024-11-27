package org.powbot.krulvis.araxxor.tree.leaf

import org.powbot.api.Locatable
import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.araxxor.Araxxor
import org.powbot.util.TransientGetter2D

class EvadeAcid(script: Araxxor) : Leaf<Araxxor>(script, "EvadeAcid") {
	override fun execute() {
		if (script.enrage) {
			script.nextEnrageTile.matrix().interact("Walk here")
			return
		}

		val flags = Movement.collisionMap(0).flags()
		val aroundMe = me.trueTile().surroundingSafeTiles(flags)
		val walkableTiles =
			if (aroundMe.size <= 2) script.araxxor.getOppositeTile(3).surroundingSafeTiles(flags) else aroundMe
		if (walkableTiles.isEmpty()) {
			script.logger.info("Cannot find safe walkable tile!")
			return
		}
		walkableTiles.minByOrNull { it.distance() }!!.matrix().interact("Walk here")
	}

	private fun Tile.surroundingSafeTiles(flags: TransientGetter2D<Int>, size: Int = 1): List<Tile> {
		val tiles = mutableListOf(this)
		for (i in 1..size) {
			tiles.add(Tile(x, y + i))
			tiles.add(Tile(x, y - i))
			tiles.add(Tile(x + i, y))
			tiles.add(Tile(x + i, y + i))
			tiles.add(Tile(x + i, y - i))
			tiles.add(Tile(x - i, y))
			tiles.add(Tile(x - i, y + i))
			tiles.add(Tile(x - i, y - i))
		}
		return tiles.filter { !it.blocked(flags) && !script.unsafeTiles.contains(it) }
	}

	private fun Locatable.getOppositeTile(max: Int = 5): Tile {
		val t = tile()
		val me = me.trueTile()
		val dx = (t.x - me.x).coerceIn(-max, max)
		val dy = (t.y - me.y).coerceIn(-max, max)
		return t.derive(dx, dy)
	}

}