package org.powbot.krulvis.tormenteddemon.tree.branch

import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.script.tree.branch.CanLoot
import org.powbot.krulvis.api.script.tree.branch.ShouldDodgeProjectile
import org.powbot.krulvis.tormenteddemon.TormentedDemon

class ShouldSwitchProtPray(script: TormentedDemon) : Branch<TormentedDemon>(script, "ShouldSwitchProtPray?") {
	override val failedComponent: TreeComponent<TormentedDemon> = CanLoot(
		script,
		ShouldDodgeProjectile(
			script,
			ShouldCastResurrect(script)
		)
	)
	override val successComponent: TreeComponent<TormentedDemon> = SimpleLeaf(script, "SwitchPray") {
		Prayer.prayer(script.protectionPrayer, true)
		prayTimer.reset()
	}

	var prayTimer = Timer(600)

	override fun validate(): Boolean {
		if (!script.currentTarget.valid() || !prayTimer.isFinished()) {
			return false
		}
		return !Prayer.prayerActive(script.protectionPrayer) && Prayer.prayerPoints() > 0
	}
}