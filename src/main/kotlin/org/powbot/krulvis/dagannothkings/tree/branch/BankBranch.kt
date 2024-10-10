package org.powbot.krulvis.dagannothkings.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.api.extensions.items.Food.Companion.hasFood
import org.powbot.krulvis.api.extensions.items.Food.Companion.needsFood
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.dagannothkings.DagannothKings
import org.powbot.krulvis.dagannothkings.tree.leaf.HandleBank

class ShouldBank(script: DagannothKings) : Branch<DagannothKings>(script, "ShouldBank?") {
	override val failedComponent: TreeComponent<DagannothKings> = AtKings(script)
	override val successComponent: TreeComponent<DagannothKings> = IsBankOpen(script)

	override fun validate(): Boolean {
		if (script.forcedBanking) return true

		val firstMissing = script.allEquipment.firstOrNull { !it.item.hasWith() }
		if (needsFood() && !hasFood()) {
			script.forcedBanking = true
		} else if (firstMissing != null) {
			val actuallyMissing = !waitFor(1000) { firstMissing.item.hasWith() }
			script.logger.info("MissingEquipment=${firstMissing.item.itemName}, actuallyMissing=$actuallyMissing")
			script.forcedBanking = actuallyMissing
		}


		return script.forcedBanking
	}
}

class IsBankOpen(script: DagannothKings) : Branch<DagannothKings>(script, "IsBankOpen?") {
	override val failedComponent: TreeComponent<DagannothKings> = SimpleLeaf(script, "OpenBank") {
		if (script.bankTeleport.execute()) {
			if (Prayer.activePrayers().isNotEmpty()) {
				Prayer.quickPrayer(true)
				sleep(450)
				Prayer.quickPrayer(false)
				waitFor { Prayer.activePrayers().isEmpty() }
			} else
				Bank.openNearest()
		}
	}
	override val successComponent: TreeComponent<DagannothKings> = HandleBank(script)

	override fun validate(): Boolean {
		return Bank.opened()
	}
}
