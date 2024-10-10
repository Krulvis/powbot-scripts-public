package org.powbot.krulvis.tormenteddemon.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.tormenteddemon.TormentedDemon

class WaitForLoot(script: TormentedDemon) : Leaf<TormentedDemon>(script, "Waiting for loot...") {
	override fun execute() {
		waitFor { script.ironmanLoot.isNotEmpty() }
	}

}