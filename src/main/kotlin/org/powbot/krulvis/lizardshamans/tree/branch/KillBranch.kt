package org.powbot.krulvis.lizardshamans.tree.branch

import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.api.script.tree.branch.ShouldHighAlch
import org.powbot.krulvis.api.script.tree.branch.ShouldSipPotion
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.lizardshamans.LizardShamans
import org.powbot.krulvis.lizardshamans.tree.leaf.AttackShaman
import org.powbot.krulvis.lizardshamans.tree.leaf.RunToSafety


class ShouldRunToSafety(script: LizardShamans) : Branch<LizardShamans>(script, "RunToSafety?") {
	override val failedComponent: TreeComponent<LizardShamans> = HascurrentTarget(script)
	override val successComponent: TreeComponent<LizardShamans> = RunToSafety(script)

	override fun validate(): Boolean {
		val destination = Movement.destination()
		val tileToCheckAgainst = if (destination.valid()) destination else me.tile()
		script.logger.info("Found ${script.spawns.size} spawns")
		return script.spawns.any { it.distanceTo(tileToCheckAgainst) <= 2 } || script.jumpEvent.shouldRun(tileToCheckAgainst) || script.currentTarget.distanceTo(tileToCheckAgainst) <= 3
	}
}

class HascurrentTarget(script: LizardShamans) : Branch<LizardShamans>(script, "HascurrentTarget?") {
	override val failedComponent: TreeComponent<LizardShamans> = AttackShaman(script)
	override val successComponent: TreeComponent<LizardShamans> = ShouldConsume(script, ShouldSipPotion(script, ShouldHighAlch(script, SimpleLeaf(script, "Chill") {
		sleep(150)
	})))

	override fun validate(): Boolean {
		return script.currentTarget.valid()
			&& (!script.currentTarget.healthBarVisible() || script.currentTarget.healthPercent() > 0)
			&& me.interacting() == script.currentTarget
	}
}

