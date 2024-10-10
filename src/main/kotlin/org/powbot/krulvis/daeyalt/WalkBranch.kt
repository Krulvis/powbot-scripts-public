package org.powbot.krulvis.daeyalt

import org.powbot.api.Notifications
import org.powbot.api.Tile
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.mobile.script.ScriptManager

class AtMine(script: DaeyaltMiner) : Branch<DaeyaltMiner>(script, "AtMine?") {
	val mineCenter = Tile(3686, 9756, 2)
	val staircaseTile = Tile(3632, 3340, 0)
	override val failedComponent: TreeComponent<DaeyaltMiner> = SimpleLeaf(script, "WalkingToMine") {
		if (Bank.opened()) {
			Bank.close()
		}
		val stairCase = Objects.stream().at(staircaseTile).name("Staircase").first()
		if (stairCase.valid() && stairCase.distance() < 14) {
			if (ATContext.walkAndInteract(stairCase, "Climb-down")) {
				Utils.waitForDistance(stairCase) { validate() }
			}
		} else if (staircaseTile.distance() > 50) {
			val medallion = Equipment.stream().name("Drakan's medallion").first()
			if (!medallion.valid()) {
				Notifications.showNotification("Can't find medallion")
				ScriptManager.stop()
			} else if (medallion.interact("Darkmeyer")) {
				Utils.waitFor(Utils.long()) { staircaseTile.distance() < 50 }
			}
		} else {
			Movement.walkTo(staircaseTile)
		}
	}
	override val successComponent: TreeComponent<DaeyaltMiner> = NextToMine(script)

	override fun validate(): Boolean {
		return mineCenter.distance() < 50
	}

}