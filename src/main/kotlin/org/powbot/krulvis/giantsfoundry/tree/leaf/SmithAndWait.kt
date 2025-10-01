package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.Condition.sleep
import org.powbot.api.Random
import org.powbot.api.rt4.Skills
import org.powbot.api.rt4.Widgets
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.mid
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.GiantsFoundry
import org.powbot.krulvis.giantsfoundry.ROOT

class SmithAndWait(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Smith and wait") {


	override fun execute() {
		val stage = script.currentStage
		val action = stage.action
		val actionObj = action.getObj()
		script.logger.info("Performing: $action, on obj=${actionObj.name}")

		var smithXp = Skills.experience(Skill.Smithing)
		if (walkAndInteract(actionObj, "Use")) {
			waitFor(long()) { Skills.experience(Skill.Smithing) > smithXp }
			smithXp = Skills.experience(Skill.Smithing)
			var lastXpGain = System.currentTimeMillis()
			while (stage.isActive() && action.canPerform() && System.currentTimeMillis() - lastXpGain <= 4000) {
				if (stage.action != script.currentStage.action) {
					script.logger.info("Stage completed, exiting smith loop")
					break
           		}
				script.logger.info("Still performing.... lastXpChange=${System.currentTimeMillis() - lastXpGain}ms")
				if (smithXp < Skills.experience(Skill.Smithing)) {
					script.logger.info("Got new experience after ${System.currentTimeMillis() - lastXpGain}ms")
					lastXpGain = System.currentTimeMillis()
					smithXp = Skills.experience(Skill.Smithing)
				}
				if (canBoost() && Random.nextDouble() > 0.2 && walkAndInteract(actionObj, "Use")) {
					waitFor(mid()) { !canBoost() || !action.canPerform() }
				}
				sleep(150)
			}
			script.logger.info(
				"Stopping SMITH" +
					"\n stageIsActive=${stage.isActive()}," +
					"\n canPerform=${action.canPerform()}," +
					"\n lastXpGain was ${System.currentTimeMillis() - lastXpGain} ms ago"
			)
			script.stopActivity(null)
		} else {
			script.logger.info("Failed to even SMITH interact...")
		}
	}

	fun canBoost(): Boolean {
		val boostComp = Widgets.component(ROOT, 4)
		if (boostComp.componentCount() > 0) {
			val boostCompChild = boostComp.component(0)
			script.logger.info("Can possibly click boost! col=${boostCompChild.textColor()}")
			return boostCompChild.textColor() == 16570115
		}
		return false
	}

}
