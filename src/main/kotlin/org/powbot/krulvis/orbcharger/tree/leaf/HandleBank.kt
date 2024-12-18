package org.powbot.krulvis.orbcharger.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.rt4.magic.RunePouch
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.orbcharger.Orb
import org.powbot.krulvis.orbcharger.Orb.Companion.COSMIC
import org.powbot.krulvis.orbcharger.Orb.Companion.UNPOWERED
import org.powbot.krulvis.orbcharger.OrbCrafter
import org.powbot.mobile.script.ScriptManager

class HandleBank(script: OrbCrafter) : Leaf<OrbCrafter>(script, "Handling Bank") {
	override fun execute() {
		val missingEquipment = script.equipment.filter { !it.meets() }
		val missingInventory = script.inventory.filter { !it.meets() }
		if (!Inventory.emptyExcept(*script.necessaries)) {
			script.logger.info(
				"Depositing because inventory contains: ${
					Inventory.stream().firstOrNull { it.id() !in script.necessaries }
				}"
			)
			Bank.depositAllExcept(*script.necessaries)
		} else if (missingEquipment.isNotEmpty()) {
			missingEquipment.forEach { it.withdrawAndEquip(true) }
		} else if (script.antipoison && !Potion.hasAntipot()) {
			script.logger.info("Needs to withdraw antipot")
			val withdraw = if (Potion.ANTIPOISON.inBank())
				Potion.ANTIPOISON.withdrawExact(1, worse = true, wait = true)
			else Potion.SUPER_ANTIPOISON.withdrawExact(1, worse = true, wait = true)
			if (!withdraw && !Potion.hasAntipotBank()) {
				script.logger.info("Out of antipots, stopping script")
				ScriptManager.stop()
			}
		} else if (missingInventory.isNotEmpty()) {
			script.logger.info("Missing Inventory: ${missingInventory.joinToString()}}")
			missingInventory.forEach { it.withdraw(false) }
		} else if (cosmicCount() < cosmicCountRequired) {
			script.logger.info("Withdrawing cosmics=$cosmicCountRequired")
			Bank.withdrawExact(COSMIC, cosmicCountRequired)
		} else if (!Inventory.isFull()) {
			script.logger.info("Withdrawing unpowered orbs")
			Bank.withdraw(UNPOWERED, Bank.Amount.ALL)
		}
	}

	fun cosmicCount(): Int {
		val pouchCount = RunePouch.runes().firstOrNull { it.first == Rune.COSMIC }?.second ?: 0
		return pouchCount + Inventory.getCount(COSMIC)
	}

	private val dontCount = arrayOf(
		Orb.UNPOWERED,
	)

	val cosmicCountRequired by lazy {
		val totalSlots = 28
		var occupiedSlots = script.inventory.count { it.item.id !in dontCount }
		if (script.antipoison) occupiedSlots++
		3 * (totalSlots - occupiedSlots)
	}

}