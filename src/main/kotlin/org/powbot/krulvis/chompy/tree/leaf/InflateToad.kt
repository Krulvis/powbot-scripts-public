package org.powbot.krulvis.chompy.tree.leaf

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.chompy.ChompyBird

class InflateToad(script: ChompyBird) : Leaf<ChompyBird>(script, "InflateToads") {
	override fun execute() {
		val toad = Npcs.stream().name("Swamp toad").nearest().first()
		val invToads = getToadCount()
		if (walkAndInteract(toad, "Inflate")) {
			if (waitForDistance(toad) { Inventory.isFull() || toad.distance() <= 1 }) {
				waitFor(3000) { Inventory.isFull() || (invToads < getToadCount() && me.animation() == -1) }
			}
		}
	}


	private fun getToadCount() = Inventory.stream().name("Bloated toad").count()
}