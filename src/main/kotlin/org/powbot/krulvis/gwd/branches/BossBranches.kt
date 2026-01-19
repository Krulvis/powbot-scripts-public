package org.powbot.krulvis.gwd.branches

import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.alive
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.gwd.GWDScript

class InsideGeneralRoom<S>(script: S) : Branch<S>(script, "InGeneralRoom?") where S : GWDScript<S> {
	override val failedComponent: TreeComponent<S> = HasKC(script)
	override val successComponent: TreeComponent<S> = ShouldConsume(script, GeneralAlive(script), false)

	override fun validate(): Boolean {
		return script.god.inside()
	}
}


class GeneralAlive<S>(script: S) : Branch<S>(script, "IsGeneralAlive?") where S : GWDScript<S> {
	override val failedComponent: TreeComponent<S> = MageAlive(script)
	override val successComponent: TreeComponent<S> = ShouldTurnOnPray(script)

	override fun validate(): Boolean {
		script.general = script.god.getGeneral()
		return script.general.alive()
	}
}

class ShouldTurnOnPray<S>(script: S) : Branch<S>(script, "ShouldTurnOnPray?") where S : GWDScript<S> {
	override val failedComponent: TreeComponent<S> = script.generalAliveBranch()
	override val successComponent: TreeComponent<S> = SimpleLeaf(script, "TurnOnPray") {
		if (Prayer.quickPrayer()) {
			Prayer.quickPrayer(false)
			sleep(100)
		}
		Prayer.quickPrayer(true)
		prayTimer.reset()
	}

	private val prayTimer = Timer(600)
	override fun validate(): Boolean {
		if (!prayTimer.isFinished()) return false
		return !Prayer.quickPrayer() || !Prayer.activePrayers().contentEquals(Prayer.quickPrayers())
	}
}