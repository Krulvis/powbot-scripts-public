package org.powbot.krulvis.araxxor.tree.leaf

import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.araxxor.Araxxor
import org.powbot.krulvis.araxxor.Data.ARAXXOR

class LootAraxxor(script: Araxxor) : Leaf<Araxxor>(script, "Harvest") {
	override fun execute() {
		val lootable = script.araxxor
		if (lootable.valid()) {
			if (walkAndInteract(lootable, "Harvest")) {
				if (waitForDistance(lootable, extraWait = 2400) {
						Npcs.stream().name(ARAXXOR).isEmpty()
					}) {
					script.banking = true
				}
			}
		}
	}

}