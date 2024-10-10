package org.powbot.krulvis.api.script.tree.leaf

import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.GroundItems
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.script.KrulScript
import org.powbot.krulvis.api.script.Looting

class Loot<S>(script: S) : Leaf<S>(script, "Looting") where S : KrulScript, S : Looting {


	override fun execute() {
		val loots = loot.sortedWith(compareBy<Pair<GroundItem, Int>> { it.first.distance() }
			.thenByDescending { it.second })
		script.logger.info("Looting=[${loots.joinToString()}]")

		loots.map { it.first }.forEachIndexed { i, gi ->
			val id = gi.id()
			val currentCount = Inventory.getCount(id)
			if ((!Inventory.isFull()
					|| gi.stackable()
					|| (Inventory.containsOneOf(Item.HERB_SACK_OPEN) && gi.name().contains("grimy", true)))
				&& walkAndInteract(gi, "Take") && (i == loots.size - 1 || gi.distance() >= 1)
			) {
				waitFor(5000) {
					currentCount < Inventory.getCount(gi.id()) || (i < loots.size - 1 && Players.local()
						.tile() == gi.tile)
				}
			}
		}
		script.ironmanLoot.removeAll { loot ->
			GroundItems.stream().at(loot.tile).name(loot.name()).none { gi -> gi.stackSize() == loot.stackSize() }
		}
		script.logger.info("Remaining loot=[${script.ironmanLoot.joinToString()}]")
	}


	companion object {
		var loot: List<Pair<GroundItem, Int>> = emptyList()
	}
}