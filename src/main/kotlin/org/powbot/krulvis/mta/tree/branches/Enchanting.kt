package org.powbot.krulvis.mta.tree.branches

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.rooms.EnchantingRoom
import org.powbot.krulvis.mta.tree.leafs.DepositOrbs
import org.powbot.krulvis.mta.tree.leafs.PickupShape

class CanCastEnchant(script: MTA) : Branch<MTA>(script, "Can Cast Enchant?") {
	override val failedComponent: TreeComponent<MTA> = ShouldDepositOrbs(script)
	override val successComponent: TreeComponent<MTA> = SimpleLeaf(script, "Casting Enchant") {
		val spell = EnchantingRoom.getEnchantSpell() ?: return@SimpleLeaf
		if (Magic.magicspell() != spell) {
			if (spell.cast()) {
				waitFor { Magic.magicspell() == spell && Game.tab() == Game.Tab.INVENTORY }
			}
		}

		sleep(150)
		val slot = alchable.inventoryIndex

		if (alchable.click()) {
			waitFor { Inventory.itemAt(slot).name() != alchable.name() }
		}
	}

	var alchable: Item = Item.Nil
	override fun validate(): Boolean {
		val bonusShape = EnchantingRoom.getBonusShape().toString()
		alchable = Inventory.stream().name(bonusShape, "Dragonstone").first()
		return alchable.valid()
	}
}


class ShouldDepositOrbs(script: MTA) : Branch<MTA>(script, "Should deposit orbs") {
	override val failedComponent: TreeComponent<MTA> = ShouldPickupDragonstone(script)
	override val successComponent: TreeComponent<MTA> = DepositOrbs(script)

	override fun validate(): Boolean {
		return Inventory.isFull()
	}
}

class ShouldPickupDragonstone(script: MTA) : Branch<MTA>(script, "E") {
	override val failedComponent: TreeComponent<MTA> = PickupShape(script)
	override val successComponent: TreeComponent<MTA> = SimpleLeaf(script, "Pickup Dragonstone") {
		if (walkAndInteract(ds, "Take")) {
			waitForDistance(ds) { getDragonStone() != ds }
		}
	}

	var ds: GroundItem = GroundItem.Nil

	private fun getDragonStone() = GroundItems.stream().name("Dragonstone").nearest().first()

	override fun validate(): Boolean {
		ds = getDragonStone()
		return ds.valid()
	}
}
