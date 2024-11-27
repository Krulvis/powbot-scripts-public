package org.powbot.krulvis.araxxor.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.araxxor.Araxxor

class ShouldBank(script: Araxxor) : Branch<Araxxor>(script, "ShouldBank?") {
	override val failedComponent: TreeComponent<Araxxor> = InLair(script)
	override val successComponent: TreeComponent<Araxxor> = BankOpen(script)

	override fun validate(): Boolean {
		if (script.banking) return true
		val missingEquipment = script.allEquipment.filter { !it.item.hasWith() }
		val missingInv = script.inventory.filter { !it.meets() }
		if (missingEquipment.isNotEmpty() || missingInv.isNotEmpty()) {
			script.logger.info("MissingEquipment=${missingEquipment.joinToString()}, missingInv=${missingInv.joinToString()}}")
			script.banking = true
		}
		return script.banking
	}
}

class BankOpen(script: Araxxor) : Branch<Araxxor>(script, "BankOpen?") {
	override val failedComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "OpenBank") {
		if (Bank.openNearest(script.bankTeleport)) {
			waitFor(long()) { Bank.opened() }
		}
	}
	override val successComponent: TreeComponent<Araxxor> = ShouldDeposit(script)

	override fun validate(): Boolean = Bank.opened()
}

class ShouldDeposit(script: Araxxor) : Branch<Araxxor>(script, "ShouldDeposit?") {
	private val potionsToFullDose =
		arrayOf(Potion.ANTI_VENOM, Potion.ANTI_VENOM_PLUS, Potion.PRAYER, Potion.SUPER_RESTORE)
	val ids by lazy {
		(script.inventory.flatMap {
			//Only keep best ID for potions that we want to bring (4) dose of
			if (it.item in potionsToFullDose) listOf((it.item as Potion).bestPot) else it.item.ids.toList()
		} + script.allEquipment.flatMap { it.item.ids.toList() }).toIntArray()
	}
	override val failedComponent: TreeComponent<Araxxor> = ShouldEquipSpec(script)

	override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "Deposit") {
		Bank.depositAllExcept(*ids)
	}

	override fun validate(): Boolean = !Inventory.emptyExcept(*ids)
}

class ShouldEquipSpec(script: Araxxor) : Branch<Araxxor>(script, "ShouldEquipSpecGear?") {
	override val failedComponent: TreeComponent<Araxxor> = ShouldGetEquipment(script)
	override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "EquipSpecGear") {
		missingEquipment.forEach { it.withdrawAndEquip(false) }
	}
	private var missingEquipment = emptyList<EquipmentRequirement>()
	override fun validate(): Boolean {
		missingEquipment = script.specEquipment.filter { !it.meets() }
		return missingEquipment.isNotEmpty()
	}
}

class ShouldGetEquipment(script: Araxxor) : Branch<Araxxor>(script, "ShouldGetEquipment?") {
	override val failedComponent: TreeComponent<Araxxor> = ShouldGetInventory(script)
	override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "GetEquipment") {
		missingEquipment.forEach { it.withdraw(false) }
	}
	private var missingEquipment = emptyList<EquipmentRequirement>()
	override fun validate(): Boolean {
		missingEquipment = script.allEquipment.filter { !it.item.hasWith() }
		return missingEquipment.isNotEmpty()
	}
}

class ShouldGetInventory(script: Araxxor) : Branch<Araxxor>(script, "ShouldGetInventory?") {
	override val failedComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "CloseBanking") {
		Bank.close()
		script.banking = false
	}
	override val successComponent: TreeComponent<Araxxor> = SimpleLeaf(script, "GetInventory") {
		if (Inventory.isFull()) {
			val food = Food.getFirstFood() ?: return@SimpleLeaf
			Bank.deposit(food.id, Bank.Amount.ALL)
		}
		missingInventory.forEach { it.withdraw(false) }
	}
	private var missingInventory = emptyList<InventoryRequirement>()
	override fun validate(): Boolean {
		missingInventory = script.inventory.filter { !it.meets() }
		return missingInventory.isNotEmpty()
	}
}
