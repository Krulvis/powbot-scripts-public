package org.powbot.krulvis.mole.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.mole.GiantMole

class Loot(script: GiantMole) : Leaf<GiantMole>(script, "Looting") {
	override fun execute() {
		script.disablePrayers()
		for (gi in script.lootList) {
			if (!gi.valid()) {
				script.lootList.remove(gi)
				return
			}

			if (walkAndInteract(gi, "Take")) {
				if (gi.distance() > 0) {
					waitForDistance(gi) { !gi.refresh().valid() }
				} else {
					sleep(100, 150)
				}
			}
		}

	}
}