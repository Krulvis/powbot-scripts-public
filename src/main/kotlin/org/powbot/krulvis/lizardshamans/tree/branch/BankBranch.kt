package org.powbot.krulvis.lizardshamans.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.lizardshamans.Data.SHAMAN_AREAS
import org.powbot.krulvis.lizardshamans.LizardShamans
import org.powbot.krulvis.lizardshamans.tree.leaf.HandleBank
import org.powbot.krulvis.lizardshamans.tree.leaf.OpenBank

class ShouldBank(script: LizardShamans) : Branch<LizardShamans>(script, "ShouldBank?") {
	override val failedComponent: TreeComponent<LizardShamans> = DoneWithTask(script)
	override val successComponent: TreeComponent<LizardShamans> = IsBankOpen(script)

	override fun validate(): Boolean {
		if (script.lootList.isNotEmpty() && SHAMAN_AREAS.any { it.contains(me) }) return false
		return script.banking || (currentHP() <= 30 && !Food.hasFood()) ||
			(Prayer.prayerPoints() < 10 && Potion.getPrayerPotion() == null)
	}
}

class IsBankOpen(script: LizardShamans) : Branch<LizardShamans>(script, "ShouldOpenBank?") {
	override val failedComponent: TreeComponent<LizardShamans> = OpenBank(script)
	override val successComponent: TreeComponent<LizardShamans> = HandleBank(script)

	override fun validate(): Boolean {
		return Bank.opened()
	}
}