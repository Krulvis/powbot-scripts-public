package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.giantsfoundry.*

class TakeMetalFromBank(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Take metal from bank") {

	override fun execute() {
		if (script.mouldWidgetOpen()) {
			val button = mouldWidget().firstOrNull { it?.text()?.contains("Set Mould") == true } ?: return
			button.click()
			waitFor { !script.mouldWidgetOpen() }
		}
		if (openBank()) {
			var barsToGet = script.barsToGet
			val crucibleBars = crucibleBars()

			script.logger.info("Getting bars=[${barsToGet.joinToString { "${it.first}:${it.second}" }}]")

			val invBars = barsToGet.map { it.first to it.first.crucibleInventoryCount() }
			script.logger.info("Already has in inventory=[${invBars.joinToString { "${it.first}:${it.second}" }}]")
			val invBarsMap = invBars.toMap()
			barsToGet = barsToGet.map { it.first to it.second - crucibleBars[it.first]!! - invBarsMap[it.first]!! }

			script.logger.info("Still need to get=[${barsToGet.joinToString { "${it.first}:${it.second}" }}]")
			barsToGet.filter { it.second > 0 }.forEach { (bar, amount) ->

				val potentialItems = METAL_ITEMS
					.filter { amount / it.value > 0 }
					.filter { script.useItems || it.key == "bar" }
					.toMap()

				script.logger.info("Potential items for ${bar.name}=[${potentialItems.keys.joinToString()}]")
				val names = potentialItems.map { it.key }.toTypedArray()
				val bankItems =
					Bank.stream().nameContains(*names).nameContains(bar.craftedBarItemPrefix(), bar.itemName)
						.filtered { it.stack > 0 }
						.sortedBy { item -> METAL_ITEM_NAMES.indexOfFirst { it in item.name() } }
				script.logger.info("bankitems=[${bankItems.joinToString { it.name() }}]")
				val item = bankItems.maxByOrNull { getCrucibleBarsForItem(it) }
					?: return

				val crucibleValue = getCrucibleBarsForItem(item)
				script.logger.info("Withdrawing ${item.name()}, crucibleValue=${crucibleValue}")
				val currentItemCount = Inventory.getCount(item.id)
				if (Bank.withdraw(item.id, amount / crucibleValue)) {
					waitFor { Inventory.getCount(item.id) >= currentItemCount }
				}
			}
		}
	}


	fun openBank(): Boolean {
		if (Bank.opened()) {
			return true
		}
		val bankObj =
			Objects.stream(30, GameObject.Type.INTERACTIVE).name("Bank chest").firstOrNull() ?: return false
		return walkAndInteract(bankObj, "Use") && waitForDistance(bankObj, extraWait = 2400) { Bank.opened() }
	}
}