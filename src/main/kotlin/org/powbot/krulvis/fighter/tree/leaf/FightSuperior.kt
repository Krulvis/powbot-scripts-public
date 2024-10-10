package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.TargetWidget
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.Superior

class FightSuperior(script: Fighter) : Leaf<Fighter>(script, "Fight Superior") {
	override fun execute() {
		val targetName = TargetWidget.name()
		val superior = Superior.forName(targetName)
		if (superior == null) {
			script.logger.info("Cannot find superior for name=${targetName}")
			return
		}
		val prayer = superior.protectPrayer
		if (prayer == Prayer.Effect.PROTECT_FROM_MELEE && !script.melee) {
			return
		}
		if (!Prayer.prayerActive(prayer)) {
			if (Prayer.prayer(prayer, true))
				waitFor { Prayer.prayerActive(prayer) }
		}
	}


}