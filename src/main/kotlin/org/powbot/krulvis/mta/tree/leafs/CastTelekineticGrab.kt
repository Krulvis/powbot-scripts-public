package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.Tile
import org.powbot.api.rt4.Camera
import org.powbot.api.rt4.Magic
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.rooms.TelekineticRoom
import org.powbot.krulvis.mta.rooms.TelekineticRoom.MAZE_GUARDIAN_MOVING
import org.powbot.krulvis.mta.rooms.TelekineticRoom.getNextMove
import org.powbot.krulvis.mta.rooms.TelekineticRoom.optimalTileForDirection
import org.powbot.krulvis.mta.rooms.TelekineticRoom.walk

class CastTelekineticGrab(script: MTA) : Leaf<MTA>(script, "Casting telekinetic grab") {

	val spell = Magic.Spell.TELEKINETIC_GRAB

	override fun execute() {
		val guardian = TelekineticRoom.mazeGuardian
		if (!guardian.inViewport(checkIfObstructed = true)) {
			script.logger.warn("Guardian not in viewport, moving camera")
			Camera.turnTo(guardian)
		}else if (!casting()) {
			if (spell.cast()) {
				waitFor { casting() }
			}
		}

		script.logger.warn("Casting=${casting()}")
		if (casting() && guardian.interact("cast")) {

			if (!waitFor(2500) { TelekineticRoom.getGuardian().id == MAZE_GUARDIAN_MOVING }) {
				script.logger.warn("Casting failed?")
				return
			}
			val nextMove = getNextMove()
			if (nextMove.first != Tile.Nil) {
				val tile = optimalTileForDirection(nextMove.second)
				script.logger.warn("Preparing for next move tile=${tile}")
				tile.walk()
			}
			script.logger.warn("Waiting for guardian to stop moving")
			val stoppedMoving = waitFor(long()) { TelekineticRoom.getGuardian().id != MAZE_GUARDIAN_MOVING }
			script.logger.warn("Guardian stopped moving = $stoppedMoving")
		}

	}

	private fun casting() = Magic.magicspell() == spell
}