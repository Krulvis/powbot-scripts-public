package org.powbot.krulvis.mole.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.HintArrow
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.mole.GiantMole

class LookForMole(script: GiantMole) : Leaf<GiantMole>(script, "Looking for mole") {
	private val centerTile = Tile(1761, 5185, 0)
	override fun execute() {
		Prayer.activePrayers().forEach {
			Prayer.prayer(it, false)
		}

		val mole = script.findMole()
		val hintTile = HintArrow.tile()
		if (mole.valid()) {
			script.logger.info("Mole is valid, walking to moleTile=${mole.tile()}")
			Movement.step(mole.tile())
			waitFor { mole.distance() <= 20 }
		} else if (hintTile.valid() && hintTile.distance() > 5 && script.respawnTimer.isFinished()) {
			script.logger.info("HintTile is valid, walking to hintTile=${hintTile}")
			Movement.step(hintTile)
			waitForDistance(hintTile, maxWait = 10000) { script.findMole().distance() <= 20 || Movement.destination().distance() <= 5 }
		} else if (centerTile.distance() > 10) {
			script.logger.info("Walking to centerTile=${centerTile}")
			Movement.step(centerTile)
			waitFor { centerTile.distance() < 10 }
		} else {
			script.logger.info("Waiting for mole to spawn...")
			waitFor { script.findMole().valid() }
		}
	}
}