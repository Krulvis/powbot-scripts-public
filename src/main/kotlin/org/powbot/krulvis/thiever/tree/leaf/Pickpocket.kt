package org.powbot.krulvis.thiever.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Constants
import org.powbot.api.rt4.Skills
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.thiever.Thiever

class Pickpocket(script: Thiever) : Leaf<Thiever>(script, "Pickpocket") {


	override fun execute() {
		val target = script.getTarget()
		if (!script.stunned() && target != null && Bank.close()) {
			val xp = Skills.experience(Constants.SKILLS_THIEVING)
			if (walkAndInteract(target, "Pickpocket", script.useMenu)) {
				waitForDistance(target) {
					target.distance() <= 1
				}
				sleep(200, 400)
			}
		}
	}
}