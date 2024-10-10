package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.DepositBox
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.extensions.items.container.Container
import org.powbot.krulvis.miner.Data
import org.powbot.krulvis.miner.Data.WATERSKINS
import org.powbot.krulvis.miner.Miner
import org.powbot.mobile.script.ScriptManager

class HandleBank(script: Miner) : Leaf<Miner>(script, "Handle Bank") {
	override fun execute() {
		if (Inventory.stream().id(*WATERSKINS, Data.EMPTY_WATERSKIN).isNotEmpty()) {
			script.waterskins = true
		}
		if (!Container.emptyAll()) {
			val failed = Container.values().filter { !it.empty() }
			script.logger.info("failed=${failed.joinToString { it.itemName }}")
			return
		} else if (!Inventory.emptyExcept(*Data.TOOLS)) {
			if (Bank.opened()) {
				Bank.depositAllExcept(*Data.TOOLS)
			} else {
				DepositBox.depositAllExcept(*Data.TOOLS)
			}
		} else if (script.waterskins && !Data.hasWaterSkins()) {
			if (Bank.containsOneOf(*WATERSKINS))
				Bank.withdraw(WATERSKINS[0], Bank.Amount.FIVE)
			else {
				script.logger.info("Out of waterskins, stopping script")
				ScriptManager.stop()
			}
		} else {
			Bank.close()
			DepositBox.close()
		}
	}
}