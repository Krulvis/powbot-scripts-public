package org.powbot.krulvis.daeyalt

import org.powbot.api.Tile
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.mta.rooms.TelekineticRoom
import kotlin.random.Random

class TickManip(script: DaeyaltMiner) : Leaf<DaeyaltMiner>(script, "TickManip") {
	override fun execute() {
		val mine = script.mine
		mine.bounds(-122, 122, -114, 0, -122, 82)
		val herb = Inventory.stream().name("Guam", "Irit", "Harralander", "Marrentill").first()
		val movableTile = getMovableTile(mine.tile) ?: return

		if (herb.valid() && herb.interact("Use")) {
			sleep(Random.nextInt(10, 50))
			movableTile.matrix().interact("Walk here")
			script.startTick = script.gameTick
			sleep(Random.nextInt(250, 450))
//			waitFor { me.movementAnimation() == 819 || script.gameTick > script.startTick + 1 }
			script.logger.info("Movement animation cycle=${me.movementAnimationCycle()}, animation=${me.movementAnimation()}")
			val startShards = Inventory.getCount(ESSENCE)
			if (Utils.walkAndInteract(mine, "Mine")) {
				sleep(Random.nextInt(250, 450))
				val tar = Inventory.stream().name("Swamp tar").first()
				if (tar.valid()) {
					tar.interact("Use")
				}
				waitFor { Inventory.getCount(ESSENCE) > startShards || script.gameTick > script.startTick + 3 }
//				script.logger.info("TickManipped animationCycle=${me.animationCycle()}, ticks=${script.gameTick - script.startTick}")
			}
		}
	}

	fun getMovableTile(mine: Tile): Tile? {
		val dir = mine.getDirection()
		val me = ATContext.me.tile()

		return when (dir) {
			TelekineticRoom.Direction.EAST, TelekineticRoom.Direction.WEST -> {
				listOf(
					Tile(me.x, mine.y + 1, me.floor),
					Tile(me.x, mine.y - 1, me.floor)
				).maxByOrNull { it.distanceTo(me) }
			}

			TelekineticRoom.Direction.NORTH, TelekineticRoom.Direction.SOUTH -> {
				listOf(
					Tile(mine.x + 1, me.y, me.floor),
					Tile(mine.x - 1, me.y, me.floor)
				).maxByOrNull { it.distanceTo(me) }
			}
		}
	}


	private fun Tile.getDirection(): TelekineticRoom.Direction {
		val me = ATContext.me.tile()
		val dx = me.x - x()
		val dy = me.y - y()
		return if (dx >= 2) {
			TelekineticRoom.Direction.WEST
		} else if (dx <= -2) {
			TelekineticRoom.Direction.EAST
		} else if (dy >= 2) {
			TelekineticRoom.Direction.NORTH
		} else {
			TelekineticRoom.Direction.SOUTH
		}
	}
}