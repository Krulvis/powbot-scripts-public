package org.powbot.krulvis.lizardshamans.tree.leaf

import org.powbot.api.Notifications
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.fighter.slayer.Slayer
import org.powbot.krulvis.lizardshamans.LizardShamans
import org.powbot.mobile.script.ScriptManager

class HandleBank(script: LizardShamans) : Leaf<LizardShamans>(script, "Handle Bank") {
	override fun execute() {
		if (Bank.depositAllExcept(*script.requiredIds)) {
			if (script.slayerTask && Slayer.taskRemainder() <= 0) {
				Notifications.showNotification("Done with slayer task, stopping script")
				ScriptManager.stop()
				return
			}
			val missingItems = missingItems()
			if (missingItems.isEmpty()) {
				script.banking = false
				Bank.close()
				return
			}

			missingItems.forEach {
				it.withdraw(false)
			}
		}

	}

	private fun missingItems(): List<InventoryRequirement> = script.inventory.filter { !it.meets() }
}