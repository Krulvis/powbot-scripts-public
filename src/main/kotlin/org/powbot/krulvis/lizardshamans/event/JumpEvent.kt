package org.powbot.krulvis.lizardshamans.event

import org.powbot.api.Tile
import org.powbot.krulvis.api.extensions.Timer

class JumpEvent(val startTile: Tile) {

	val timer = Timer(5000)

	fun shouldRun(tileToCheckAgainst: Tile): Boolean {
		return !timer.isFinished() && tileToCheckAgainst.distanceTo(startTile) <= 2
	}

	override fun toString() = "JumpEvent(remainder=${Timer.formatTime(timer.getRemainder(), true)})"
}