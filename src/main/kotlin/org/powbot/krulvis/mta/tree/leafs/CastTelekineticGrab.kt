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
		if (!guardian.inViewport()) {
			script.logger.info("Guardian not in viewport, moving camera")
			Camera.turnTo(guardian)
		}
		if (!casting()) {
			if (spell.cast()) {
				waitFor { casting() }
			}
		}

		script.logger.info("Casting=${casting()}")
		if (casting() && guardian.interact("cast")) {
			val flags = TelekineticRoom.getFlags()

			if (!waitFor(2500) { TelekineticRoom.getGuardian(flags).id == MAZE_GUARDIAN_MOVING }) {
				script.logger.info("Casting failed?")
				return
			}
			val nextMove = getNextMove()
			if (nextMove.first != Tile.Nil) {
				val tile = optimalTileForDirection(nextMove.second)
				script.logger.info("Preparing for next move tile=${tile}")
				tile.walk()
			}
			script.logger.info("Waiting for guardian to stop moving")
			val stoppedMoving = waitFor(long()) { TelekineticRoom.getGuardian(flags).id != MAZE_GUARDIAN_MOVING }
			script.logger.info("Guardian stopped moving = $stoppedMoving")
		}

	}

	private fun casting() = Magic.magicspell() == spell
}