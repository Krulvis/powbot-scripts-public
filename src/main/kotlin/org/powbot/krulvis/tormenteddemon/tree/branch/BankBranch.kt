package org.powbot.krulvis.tormenteddemon.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.tormenteddemon.TormentedDemon
import org.powbot.krulvis.tormenteddemon.tree.leaf.HandleBank
import org.powbot.krulvis.tormenteddemon.tree.leaf.OpenBank

class ShouldBank(script: TormentedDemon) : Branch<TormentedDemon>(script, "Should Bank") {
	override val successComponent: TreeComponent<TormentedDemon> = IsBankOpen(script)
	override val failedComponent: TreeComponent<TormentedDemon> = ShouldSwitchProtPray(script)

	override fun validate(): Boolean {
		if (script.forcedBanking) return true

		val ammo = script.ammos
		if (ammo.isNotEmpty() && ammo.none { it.inEquipment() }) return true

		return !Food.hasFood() && (
			(script.currentTarget.valid() && Food.needsFood()) || Bank.opened() || (Inventory.isFull() && !Potion.PRAYER.hasWith())
			)
	}
}

class IsBankOpen(script: TormentedDemon) : Branch<TormentedDemon>(script, "Is Bank Open") {
	override val successComponent: TreeComponent<TormentedDemon> = HandleBank(script)
	override val failedComponent: TreeComponent<TormentedDemon> = OpenBank(script)
	override fun validate(): Boolean {
		return Bank.opened()
	}
}