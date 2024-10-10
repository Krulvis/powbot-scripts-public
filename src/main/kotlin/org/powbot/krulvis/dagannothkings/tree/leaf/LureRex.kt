package org.powbot.krulvis.dagannothkings.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.dagannothkings.DagannothKings
import org.powbot.krulvis.dagannothkings.Data

class LureRex(script: DagannothKings) : Leaf<DagannothKings>(script, "LuringRex") {
	override fun execute() {
		val rex = script.target.tile()
		val ladderTile = Data.getKingsLadderDown().tile
		val deltaY = rex.y - ladderTile.y
		script.logger.info("Distance to rex=${rex.distance()}, deltaY=${deltaY}")
		if (deltaY < -4 || rex.distance() > 8) {
			//Have to stand on lureTile
			if (script.lureTile.distance() > 0) {
				Movement.step(script.lureTile, 0)
			}
		} else if (script.safeTile.distance() > 0) {
			Movement.step(script.safeTile)
			waitFor(2500) { script.safeTile.distance() == 0.0 }
		}
	}

}