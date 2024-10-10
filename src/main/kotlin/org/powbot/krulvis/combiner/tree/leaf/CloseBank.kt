package org.powbot.krulvis.combiner.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.combiner.Combiner

class CloseBank(script: Combiner) : Leaf<Combiner>(script, "ClosingBank") {
	override fun execute() {
		if (Bank.close()) {
			waitFor { !Bank.opened() }
		}
	}
}