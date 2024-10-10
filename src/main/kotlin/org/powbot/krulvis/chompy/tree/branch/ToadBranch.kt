package org.powbot.krulvis.chompy.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.chompy.ChompyBird
import org.powbot.krulvis.chompy.tree.leaf.DropToad
import org.powbot.krulvis.chompy.tree.leaf.InflateToad
import org.powbot.krulvis.chompy.tree.leaf.SuckBubbles

class HasToad(script: ChompyBird) : Branch<ChompyBird>(script, "CanSetupToad") {
	override val failedComponent: TreeComponent<ChompyBird> = CanInflate(script)
	override val successComponent: TreeComponent<ChompyBird> = DropToad(script)

	override fun validate(): Boolean {
		val bloated = Inventory.stream().name("Bloated toad").count().toInt()
		if (bloated == 0) {
			script.placingToads = false
		} else if (Inventory.isFull() || bloated >= 3) {
			script.placingToads = true
		}
		return script.placingToads
	}
}

class CanInflate(script: ChompyBird) : Branch<ChompyBird>(script, "CanInflate") {
	override val failedComponent: TreeComponent<ChompyBird> = SuckBubbles(script)
	override val successComponent: TreeComponent<ChompyBird> = InflateToad(script)

	private val filledIds = intArrayOf(2872, 2873, 2874)
	override fun validate(): Boolean {
		return Inventory.stream().id(*filledIds).isNotEmpty() && !Inventory.isFull()
	}
}