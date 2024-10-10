package org.powbot.krulvis.api.extensions.requirements

import org.powbot.api.requirement.ItemRequirement
import org.powbot.api.requirement.Requirement
import org.powbot.api.rt4.Bank
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.ITeleportItem
import org.powbot.krulvis.api.extensions.items.Item

interface ItemRequirement : Requirement {

	val item: Item
	var amount: Int

	/**
	 * Handles banking
	 */
	fun withdraw(wait: Boolean): Boolean {
		if (item is ITeleportItem) {
			item.withdrawExact(amount, true)
		} else if (item.stackable && amount > 28) {
			Bank.withdraw(item.getBankId(), Bank.Amount.ALL)
			if (wait) waitFor { meets() }
		}
		return item.withdrawExact(amount, wait = wait)
	}

	companion object {
		fun List<ItemRequirement>.ids() = flatMap { it.ids.toList() }.toIntArray()
	}

}