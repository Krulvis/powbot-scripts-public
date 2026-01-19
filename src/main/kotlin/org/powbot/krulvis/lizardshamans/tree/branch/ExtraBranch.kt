package org.powbot.krulvis.lizardshamans.tree.branch

import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.krulvis.api.script.tree.branch.CanLoot
import org.powbot.krulvis.lizardshamans.Data.SHAMAN_AREAS
import org.powbot.krulvis.lizardshamans.LizardShamans
import org.powbot.krulvis.lizardshamans.tree.leaf.GoToShamans

class AtShamans(script: LizardShamans) : Branch<LizardShamans>(script, "AtShamans?") {
	override val failedComponent: TreeComponent<LizardShamans> = GoToShamans(script)
	override val successComponent: TreeComponent<LizardShamans> = IsPrayOn(script)

	override fun validate(): Boolean {
		return SHAMAN_AREAS.any { it.contains(me) }
	}
}

class IsPrayOn(script: LizardShamans) : Branch<LizardShamans>(script, "IsPrayOn?") {
	override val failedComponent: TreeComponent<LizardShamans> = SimpleLeaf(script, "TurnPrayOn") {
		if (Prayer.quickPrayer(true)) {
			Utils.waitFor { Prayer.quickPrayer() }
		}
	}
	override val successComponent: TreeComponent<LizardShamans> = CanLoot(script, ShouldRunToSafety(script))

	override fun validate(): Boolean {
		return Prayer.quickPrayer()
	}
}
