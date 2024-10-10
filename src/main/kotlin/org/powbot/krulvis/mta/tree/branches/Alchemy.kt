package org.powbot.krulvis.mta.tree.branches

import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.rooms.AlchemyRoom
import org.powbot.krulvis.mta.tree.leafs.CastHighAlch
import org.powbot.krulvis.mta.tree.leafs.SearchCupboard

class CanCastHA(script: MTA) : Branch<MTA>(script, "Can Cast HA?") {
	override val failedComponent: TreeComponent<MTA> = IsItemOnGround(script)
	override val successComponent: TreeComponent<MTA> = CastHighAlch(script)

	var lastBest: AlchemyRoom.Alchable = AlchemyRoom.Alchable.NONE
	override fun validate(): Boolean {
		AlchemyRoom.bestItem = AlchemyRoom.getBest()

		script.logger.info("Best item = ${AlchemyRoom.bestItem}, changed=${lastBest != AlchemyRoom.bestItem}")
		if (lastBest != AlchemyRoom.bestItem) {
			AlchemyRoom.order = emptyList()
			lastBest = AlchemyRoom.bestItem
		}
		return AlchemyRoom.bestItem.inventoryItem().valid()
	}
}

class IsItemOnGround(script: MTA) : Branch<MTA>(script, "Is item on ground?") {
	override val failedComponent: TreeComponent<MTA> = ShouldDrop(script)
	override val successComponent: TreeComponent<MTA> = SimpleLeaf(script, "Picking groundItem") {
		if (groundItem.interact("Take")) {
			waitForDistance(groundItem) { AlchemyRoom.bestItem.inventoryItem().valid() }
		}
	}

	var groundItem: GroundItem = GroundItem.Nil

	override fun validate(): Boolean {
		groundItem = AlchemyRoom.bestItem.groundItem()
		return groundItem.valid() && !Inventory.isFull()
	}
}

class ShouldDrop(script: MTA) : Branch<MTA>(script, "Should drop items?") {
	override val failedComponent: TreeComponent<MTA> = ShouldSearch(script)
	override val successComponent: TreeComponent<MTA> = SimpleLeaf(script, "Dropping") {
		droppables.forEach { it.interact("Drop") }
		waitFor(600) { AlchemyRoom.getDroppables().isEmpty() }
	}

	var droppables: List<Item> = emptyList()

	override fun validate(): Boolean {
		droppables = AlchemyRoom.getDroppables()
		script.logger.info("droppables=${droppables.count()}")
		return droppables.isNotEmpty()
	}
}


class ShouldSearch(script: MTA) : Branch<MTA>(script, "Should search cupboard?") {
	override val failedComponent: TreeComponent<MTA> = SimpleLeaf(script, "Taking from cupboard") {
		val cupboard = AlchemyRoom.getCupboard()
		val emptySlots = Inventory.emptySlotCount()
		if (walkAndInteract(cupboard, "Take-5")) {
			if (waitFor { emptySlots > Inventory.emptySlotCount() }) {

			}
		}
	}
	override val successComponent: TreeComponent<MTA> = SearchCupboard(script)

	override fun validate(): Boolean {
		return AlchemyRoom.order.isEmpty()
	}
}