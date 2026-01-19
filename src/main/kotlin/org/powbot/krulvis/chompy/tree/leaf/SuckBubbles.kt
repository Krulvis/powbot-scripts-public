package org.powbot.krulvis.chompy.tree.leaf

import org.powbot.api.Production.stoppedMaking
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.mid
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.chompy.ChompyBird

class SuckBubbles(script: ChompyBird) : Leaf<ChompyBird>(script, "SuckBubbles") {
	override fun execute() {
		val swampBubbles = Objects.stream().name("Swamp bubbles").action("Suck")
			.filtered { it.tile().getWalkableNeighbor(false, false, false) != null }
			.nearest().first()
		if (walkAndInteract(swampBubbles, "Suck")) {
			if (waitForDistance(swampBubbles) { swampBubbles.distance() <= 1 }) {
				if (waitFor(mid()) { !stoppedMaking(2872) }) {
					waitFor(60000) {
						stoppedMaking(2872) || !Inventory.containsOneOf(2871, 2873, 2874)
							|| script.getAttackableBird().valid()
					}
				}
			}
		}
	}
}