package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.Notifications
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.missingHP
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.container.Container
import org.powbot.krulvis.fighter.Defender
import org.powbot.krulvis.fighter.Fighter
import org.powbot.mobile.script.ScriptManager

class HandleBank(script: Fighter) : Leaf<Fighter>(script, "Handle bank") {
	override fun execute() {
		if (script.lastTrip) {
			Notifications.showNotification("Stopping because this was the last trip")
			ScriptManager.stop()
			return
		}
		script.bankTeleport.executed = false
		val ids = script.requiredInventory.flatMap { it.item.ids.toList() }.toIntArray()
		val edibleFood = foodToEat()
		val defender = Defender.defender()
		if (!Inventory.emptyExcept(Defender.defenderId(), *ids, script.warriorTokens)) {
			Bank.depositInventory()
		} else if (edibleFood != null) {
			edibleFood.eat()
			waitFor { foodToEat() == null }
		} else if (Defender.defenderId() > 0 && !defender.hasWith()) {
			defender.withdrawExact(1)
		} else if (script.warriorGuild && !Inventory.containsOneOf(script.warriorTokens)) {
			if (!Bank.withdraw(script.warriorTokens, Bank.Amount.ALL) && !Bank.containsOneOf(script.warriorTokens)) {
				script.logger.info("Out of warrior tokens, stopping script")
				ScriptManager.stop()
			}
		} else if (!Container.emptyAll()) {
			val failed = Container.values().filter { !it.empty() }
			script.logger.info("failed=${failed.joinToString { it.itemName }}")
			return
		} else if (handleEquipment() && foodToEat() == null) {

			val missingItem = script.requiredInventory.filter { !it.meets() }
			missingItem.forEach {
				if (!it.withdraw(true) && !it.item.inBank()) {
					script.logger.info("Stopped because no ${it.item.itemName} in bank")
					ScriptManager.stop()
				}
			}

			script.logger.info("Missing Items=[${missingItem.joinToString { it.item.itemName }}]")

			script.forcedBanking = missingItem.any { !it.meets() }
			if (!script.forcedBanking) Bank.close()
		} else {
			script.logger.info("Handling equipment not finished...")
		}

	}

	fun foodToEat() = Food.values().firstOrNull { it.inInventory() && it.healing <= missingHP() }

	fun handleEquipment(): Boolean {
		script.equipment.forEach {
			if (!it.meets()) {
				it.withdrawAndEquip(true)
			}
		}
		script.equipment.forEach {
			if (!it.meets()) {
				it.item.withdrawExact(1, true, wait = true)
			}
		}
		script.teleportEquipments.forEach {
			if (!it.hasWith()) {
				it.withdrawExact(1, true, wait = true)
			}
		}
		return script.equipment.all { it.meets() } && script.teleportEquipments.all { it.hasWith() }
	}
}