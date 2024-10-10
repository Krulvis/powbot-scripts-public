package org.powbot.krulvis.dagannothkings.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.dagannothkings.DagannothKings

class HandleBank(script: DagannothKings) : Leaf<DagannothKings>(script, "HandleBank") {
	override fun execute() {
		val names = script.allEquipment.map { it.item.itemName } + script.inventory.map { it.item.itemName }
		val depositables = Inventory.get { item -> names.none { n -> n.contains(item.name()) || item.name().contains(n) } }
		val missingGear = script.allEquipment.filter { !it.item.hasWith() }
		if (depositables.isNotEmpty()) {
			script.logger.info("Depositing inventory because found depositables=[${depositables.joinToString()}]")
			Bank.depositInventory()
		} else if (missingGear.isNotEmpty()) {
			missingGear.forEach { it.withdrawAndEquip(true) }
		} else if (script.inventory.all { it.withdraw(true) }) {
			script.forcedBanking = false
		}
	}
}