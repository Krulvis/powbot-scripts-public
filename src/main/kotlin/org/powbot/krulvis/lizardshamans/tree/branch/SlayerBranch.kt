package org.powbot.krulvis.lizardshamans.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Utils.mid
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.fighter.slayer.Slayer
import org.powbot.krulvis.lizardshamans.LizardShamans

class DoneWithTask(script: LizardShamans) : Branch<LizardShamans>(script, "DoneWithTask?") {
	override val failedComponent: TreeComponent<LizardShamans> = AtShamans(script)
	override val successComponent: TreeComponent<LizardShamans> = SimpleLeaf(script, "StoppingScript") {
		if (!waitFor(mid()) { script.lootList.isNotEmpty() })
			script.banking = true
	}

	override fun validate(): Boolean {
		return script.slayerTask && Slayer.taskRemainder() <= 0 && script.lootList.isEmpty()
	}
}