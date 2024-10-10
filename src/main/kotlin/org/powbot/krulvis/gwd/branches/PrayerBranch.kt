package org.powbot.krulvis.gwd.branches

import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.gwd.GWDScript

class ShouldProtPray<S>(
	script: S,
	private val prayer: Prayer.Effect,
	override val failedComponent: TreeComponent<S>
) :
	Branch<S>(script, "ShouldProtPray?") where S : GWDScript<S> {
	override val successComponent = SimpleLeaf(script, "ProtPray") {
		Prayer.prayer(prayer, true)
		prayTimer.reset()
	}

	private val prayTimer = Timer(600)
	override fun validate(): Boolean {
		if (!prayTimer.isFinished()) return false
		return !Prayer.prayerActive(prayer)
	}
}