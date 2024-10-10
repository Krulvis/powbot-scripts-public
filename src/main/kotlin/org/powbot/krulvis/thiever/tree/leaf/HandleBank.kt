package org.powbot.krulvis.thiever.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.missingHP
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.thiever.Thiever
import java.lang.Integer.min

class HandleBank(script: Thiever) : Leaf<Thiever>(script, "Handle Bank") {
	override fun execute() {
		if (script.coinPouch() != null) {
			Bank.close()
			return
		}

		val missingPieces = script.getMissingRoguesPieces()
		if (missingPieces.isNotEmpty()) {
			missingPieces.forEach { Bank.withdraw(it, 1) }
			missingPieces.forEach { Inventory.stream().name(it).first().interact("Wear") }
			waitFor(1000) { script.getMissingRoguesPieces().isEmpty() }
		}

		val toHeal = missingHP() / script.food.healing
		val toTakeTotal = min(28, script.foodAmount + toHeal)
		if (!Inventory.emptyExcept(*script.food.ids)) {
			Bank.depositInventory()
			waitFor { !Inventory.isFull() }
		}

		if (script.food.getInventoryCount() < toTakeTotal) {
			script.logger.info("Taking out: $toTakeTotal, extra=$toHeal")
			if (Bank.withdraw(script.food.getBankId(), toTakeTotal)) {
				waitFor { script.food.inInventory() }
			}
		} else if (script.dodgyNeck && !script.dodgy.hasWith()) {
			if (!script.dodgy.withdrawAndEquip(false)) {
				if (!script.dodgy.inInventory() && !script.dodgy.inBank()) {
					script.dodgyNeck = false
				}
			}
		}
	}
}