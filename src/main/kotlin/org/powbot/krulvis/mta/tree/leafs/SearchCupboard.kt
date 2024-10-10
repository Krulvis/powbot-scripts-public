package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.rooms.AlchemyRoom

class SearchCupboard(script: MTA) : Leaf<MTA>(script, "Searching cupboard") {
	override fun execute() {
		val cupboards = AlchemyRoom.getAllCupboards()
		val nearest = cupboards.minByOrNull { it.distance() } ?: return

		val inventory = Inventory.itemCounts()
		if (walkAndInteract(nearest, "Search")) {
			val index = cupboards.indexOf(nearest)
			waitFor { inventory != Inventory.itemCounts() }
			val foundItem = Inventory.itemCounts().firstOrNull { !inventory.contains(it) }?.first
				?: AlchemyRoom.Alchable.NONE.itemName
			val alchable = AlchemyRoom.Alchable.forName(foundItem)
			script.logger.info("New item found=$alchable, at pole with id=${nearest.id}, and index=$index")

			val newOrder = buildOrder(alchable, index)
			script.logger.info(newOrder.joinToString())
			AlchemyRoom.order = newOrder
		}
	}

	private fun buildOrder(alchable: AlchemyRoom.Alchable, cupboardIndex: Int): List<AlchemyRoom.Alchable> {
		val alchables = AlchemyRoom.Alchable.values()
		val knownAlchableIndex = alchable.ordinal
		val offset = (knownAlchableIndex - cupboardIndex + alchables.size) % alchables.size
		script.logger.info("CupboardIndex=$cupboardIndex, knownItemIndex=$knownAlchableIndex, offset=$offset")

		// Rotate the list based on the offset
		return alchables.drop(offset) + alchables.take(offset)
	}

	private fun Inventory.itemCounts() =
		items().groupBy { it.name() }.map { it.key to it.value.sumOf { item -> item.stack } }
}