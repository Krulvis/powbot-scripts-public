package org.powbot.krulvis.combiner.tree.branch

import org.powbot.api.Random
import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.combiner.Combiner
import org.powbot.krulvis.combiner.tree.leaf.CloseBank
import org.powbot.krulvis.combiner.tree.leaf.Combine
import org.powbot.krulvis.combiner.tree.leaf.HandleBank

class ShouldBank(
	script: Combiner
) : Branch<Combiner>(script, "Should bank") {
	override val failedComponent: TreeComponent<Combiner> = StoppedCombining(script)
	override val successComponent: TreeComponent<Combiner> = HandleBank(script)

	override fun validate(): Boolean {
		return script.shouldBank()
	}
}

class StoppedCombining(
	script: Combiner
) : Branch<Combiner>(script, "Stopped Combining") {
	override val failedComponent: TreeComponent<Combiner> =
		SimpleLeaf(script, "Chilling") { sleep(Random.nextInt(600, 1000)) }
	override val successComponent: TreeComponent<Combiner> = ShouldCloseBank(script)

	override fun validate(): Boolean {
		return script.spamClick || script.stoppedUsing()
	}
}

class ShouldCloseBank(
	script: Combiner
) : Branch<Combiner>(script, "ShouldCloseBank") {
	override val failedComponent: TreeComponent<Combiner> = Combine(script)
	override val successComponent: TreeComponent<Combiner> = CloseBank(script)

	override fun validate(): Boolean {
		return Bank.opened()
	}
}