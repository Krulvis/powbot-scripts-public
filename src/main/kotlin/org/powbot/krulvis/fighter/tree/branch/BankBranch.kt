package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.missingHP
import org.powbot.krulvis.api.extensions.Cannon
import org.powbot.krulvis.api.extensions.items.EquipmentItem
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.items.RunePouch
import org.powbot.krulvis.api.extensions.items.container.Container
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.script.tree.branch.CanLoot
import org.powbot.krulvis.api.script.tree.branch.ShouldSetRunePouchRunes
import org.powbot.krulvis.fighter.Defender
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.OpenBank
import org.powbot.mobile.script.ScriptManager

class ShouldBank(script: Fighter) : Branch<Fighter>(script, "Should Bank") {
	override val successComponent: TreeComponent<Fighter> = IsBankOpen(script)
	override val failedComponent: TreeComponent<Fighter> = CanLoot(script, ShouldPrayAtAltar(script))

	override fun validate(): Boolean {
		if (script.forcedBanking) return true

		val ammo = script.ammo
		if (ammo != null && !ammo.item.hasWith()) return true

		return !Food.hasFood() && (
			Food.needsFood() || Bank.opened() || (Inventory.isFull() && !Potion.PRAYER.hasWith())
			)
	}
}

class IsBankOpen(script: Fighter) : Branch<Fighter>(script, "Is Bank Open") {
	override val successComponent: TreeComponent<Fighter> = ShouldDeposit(script)
	override val failedComponent: TreeComponent<Fighter> = OpenBank(script)
	override fun validate(): Boolean {
		return Bank.opened()
	}
}

class ShouldDeposit(script: Fighter) : Branch<Fighter>(script, "Should Deposit?") {
	override val failedComponent: TreeComponent<Fighter> = ShouldEat(script)
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Deposit Inventory") {
		Bank.depositInventory()
	}

	override fun validate(): Boolean {
		val ids = script.requiredInventory.flatMap { it.item.ids.toList() }.toIntArray()
		return !Inventory.emptyExcept(Defender.defenderId(), *ids, script.warriorTokens)
	}
}

class ShouldEat(script: Fighter) : Branch<Fighter>(script, "Should Eat?") {
	override val failedComponent: TreeComponent<Fighter> = ShouldWithdrawDefender(script)
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Eat") {
		edibleFood?.eat()
	}

	private var edibleFood: Food? = null
	override fun validate(): Boolean {
		edibleFood = Food.values().firstOrNull { it.inInventory() && it.healing <= missingHP() }
		return edibleFood != null
	}

}

class ShouldWithdrawDefender(script: Fighter) : Branch<Fighter>(script, "ShouldWithdrawDefender?") {
	override val failedComponent: TreeComponent<Fighter> = ShouldwithdrawTokens(script)
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "WithdrawDefender") {
		defender.withdrawExact(1)
	}

	private var defender: EquipmentItem = EquipmentItem.Nil
	override fun validate(): Boolean {
		defender = Defender.defender()
		return defender.id > 0 && !defender.hasWith()
	}

}

class ShouldwithdrawTokens(script: Fighter) : Branch<Fighter>(script, "ShouldWithdrawTokens?") {
	override val failedComponent: TreeComponent<Fighter> = ShouldWithdrawEquipment(script)
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "WithdrawTokens") {
		if (!Bank.withdraw(script.warriorTokens, Bank.Amount.ALL) && !Bank.containsOneOf(script.warriorTokens)) {
			script.logger.info("Out of warrior tokens, stopping script")
			ScriptManager.stop()
		}
	}

	private var defender: EquipmentItem = EquipmentItem.Nil
	override fun validate(): Boolean {
		return script.warriorGuild && !Inventory.containsOneOf(script.warriorTokens)
	}

}

class ShouldWithdrawEquipment(script: Fighter) : Branch<Fighter>(script, "ShouldWithdrawEquipment?") {
	override val failedComponent: TreeComponent<Fighter> = ShouldWithdrawRunePouch(script)

	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "WithdrawEquipment") {
		missingEquipment.forEach {
			if (!it.meets()) {
				it.withdrawAndEquip(true)
			}
		}
	}

	private var missingEquipment: List<EquipmentRequirement> = emptyList()
	override fun validate(): Boolean {
		missingEquipment = script.equipment.filter { !it.meets() }
		return script.warriorGuild && !Inventory.containsOneOf(script.warriorTokens)
	}

}

class ShouldWithdrawRunePouch(script: Fighter) : Branch<Fighter>(script, "ShouldWithdrawRunePouch?") {
	override val failedComponent: TreeComponent<Fighter> = ShouldSetRunePouchRunes(script, ShouldWithdrawItems(script))

	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "WithdrawRunePouch") {
		RunePouch.withdrawExact(1)
	}

	override fun validate(): Boolean {
		return script.requiredInventory.any { RunePouch.isRunePouch(it.item) && !it.meets() }
	}
}

class ShouldWithdrawItems(script: Fighter) : Branch<Fighter>(script, "ShouldWithdrawItems?") {
	override val failedComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Empty Containers") {
		if (Container.emptyAll()) {
			script.forcedBanking = false
		}
	}

	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "WithdrawItems") {
		missingItems.forEach {
			if (!it.withdraw(true) && !it.item.inBank()) {
				val missing = "Stopped because no ${it.item.itemName} in bank"
				script.logger.info(missing)
				Notifications.showNotification(missing)
				ScriptManager.stop()
			}
		}
	}

	private var missingItems: List<InventoryRequirement> = emptyList()
	override fun validate(): Boolean {
		val cannonPlaced = Cannon.placed()
		missingItems = script.requiredInventory
			.filter { !it.meets() && (!cannonPlaced || !Cannon.items.contains(it.item.id)) }
		return missingItems.isNotEmpty()
	}
}