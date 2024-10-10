package org.powbot.krulvis.lizardshamans.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.mid
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.lizardshamans.LizardShamans

class RunToSafety(script: LizardShamans) : Leaf<LizardShamans>(script, "RunToSafety") {
	override fun execute() {
		val escapeTile = script.escapeTile

		script.logger.info("Stepping to escapeTile=${escapeTile}")
		val startRunning = Timer()
		if (!Movement.running() && Movement.energyLevel() > 1) {
			Movement.running(true)
		}
		if (Movement.step(escapeTile, 0)) {
			script.logger.info("Ran to escapetile to run from spawns within $startRunning")
			waitFor(mid()) { escapeTile.distance() <= 2 }
		}
	}
}