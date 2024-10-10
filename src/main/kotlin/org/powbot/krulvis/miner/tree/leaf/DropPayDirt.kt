package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.canReach
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.miner.Miner

class DropPayDirt(script: Miner) : Leaf<Miner>(script, "Drop pay-dirt") {

	override fun execute() {
		val hopper = getHopper()
		val totalPaydirt = Inventory.getCount(Ore.PAY_DIRT.id) + script.getMotherlodeCount()
		if (hopper != null && hopper.canReach()) {
			if (walkAndInteract(hopper, "Deposit")) {
				script.lastPayDirtDrop = System.currentTimeMillis()
				sleep(600)
				waitFor(long()) {
					deposited() && canWalkAway()
				}
			}
		} else if (!script.nearHopper.reachable() || script.nearHopper.distance() > 15) {
			if (!script.escapeTopFloor()) return
			val path = LocalPathFinder.findPath(script.nearHopper)
			if (path.isNotEmpty()) {
				path.traverseUntilReached()
			} else {
				Movement.moveTo(script.nearHopper)
			}
		}

		if (totalPaydirt >= script.getMotherlodeCapacity() && deposited()) {
			script.shouldEmptySack = true
		}
	}

	fun canWalkAway(): Boolean {
		return script.getBrokenStrut() == null
			|| Npcs.stream().name("Pay-dirt").isNotEmpty()
	}

	fun deposited() = Inventory.getCount(Ore.PAY_DIRT.id) == 0

	fun getHopper() = Objects.stream(50).type(GameObject.Type.INTERACTIVE)
		.at(if (script.topLevelHopper) Tile(3755, 5677, 0) else Tile(3748, 5672, 0))
		.name("Hopper").action("Deposit").nearest().firstOrNull()
}