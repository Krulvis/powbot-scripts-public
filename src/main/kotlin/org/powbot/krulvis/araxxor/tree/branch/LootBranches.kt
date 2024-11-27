package org.powbot.krulvis.araxxor.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.araxxor.Araxxor
import org.powbot.krulvis.araxxor.tree.leaf.LootAraxxor

class CanLoot(script: Araxxor) : Branch<Araxxor>(script, "CanLoot?") {
	override val failedComponent: TreeComponent<Araxxor> = ShouldBank(script)
	override val successComponent: TreeComponent<Araxxor> = LootAraxxor(script)

	override fun validate(): Boolean {
		return script.araxxor.actions().any { it.contains("Harvest", true) }
	}
}