package org.powbot.krulvis.gwd.leaf

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Prayer
import org.powbot.api.rt4.Skills
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.gwd.GWDScript

class PrayAtAltar<S>(script: S, val altarTimer: Timer) : Leaf<S>(script, "PrayAtAltar") where S : GWDScript<S> {
	override fun execute() {
		val altar =
			Objects.stream().type(GameObject.Type.INTERACTIVE).name(script.god.name + " altar").action("Pray").first()
		if (walkAndInteract(altar, "Pray")) {
			if(waitForDistance(altar) { Prayer.prayerPoints() == Skills.realLevel(Skill.Prayer) }){
				altarTimer.reset()
			}
		}
	}
}