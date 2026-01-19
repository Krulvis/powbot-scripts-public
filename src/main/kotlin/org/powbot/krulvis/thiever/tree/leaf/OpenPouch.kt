package org.powbot.krulvis.thiever.tree.leaf

import org.powbot.api.Random
import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.thiever.Thiever

class OpenPouch(script: Thiever) : Leaf<Thiever>(script, "Pouches") {
	override fun execute() {
		Bank.close()
		if (script.coinPouch()?.interact("Open-all") == true) {
			script.nextPouchOpening = Random.nextInt(1, 28)
			waitFor { script.coinPouch() == null }
		}
	}
}