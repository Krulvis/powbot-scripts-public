package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter

class WaitForLoot(script: Fighter) : Leaf<Fighter>(script, "Waiting for loot...") {
	override fun execute() {
		if (script.canDeactivateQuickPrayer()) {
			Prayer.quickPrayer(false)
		}
		if (!script.aggressionTimer.isFinished() && script.shouldReturnToSafespot()) {
			Movement.step(script.centerTile, 0)
		} else {
			waitFor { script.getGroundLoot().isNotEmpty() }
		}
	}

}