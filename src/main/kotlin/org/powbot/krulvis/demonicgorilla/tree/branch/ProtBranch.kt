package org.powbot.krulvis.demonicgorilla.tree.branch

import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.script.tree.branch.CanLoot
import org.powbot.krulvis.api.script.tree.branch.ShouldDodgeProjectile
import org.powbot.krulvis.demonicgorilla.DemonicGorilla

class ShouldSwitchProtPray(script: DemonicGorilla) : Branch<DemonicGorilla>(script, "ShouldSwitchProtPray?") {
	override val failedComponent: TreeComponent<DemonicGorilla> =
		CanLoot(script, ShouldDodgeProjectile(script, ShouldCastResurrect(script)))
	override val successComponent: TreeComponent<DemonicGorilla> = SimpleLeaf(script, "SwitchPray") {
		Prayer.prayer(script.protectionPrayer, true)
		protTimer.reset()
	}

	val protTimer = Timer(600)

	override fun validate(): Boolean {
		if (!script.currentTarget.valid() || !protTimer.isFinished()) {
			return false
		}
		return script.currentTarget.valid() && !Prayer.prayerActive(script.protectionPrayer) && Prayer.prayerPoints() > 0
	}
}