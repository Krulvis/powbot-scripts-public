package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.Notifications
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.tithe.TitheFarmer
import org.powbot.mobile.script.ScriptManager

class Plant(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Planting") {

	override fun execute() {
		if (script.lastRound) {
			Notifications.showNotification("Stopped script because it was the last round!")
			ScriptManager.stop()
		}
		val hasSeeds = script.hasSeeds()
		val patch = script.patches.firstOrNull { it.isEmpty() }
		if (!hasSeeds || patch == null) {
			script.logger.warn("Stopped planting: hasSeeds=$hasSeeds, patch=$patch")
			script.planting = false
		} else {
			script.planting = true
			val seed = script.getSeed()
			val walkBetween = patch.walkBetween(script.patches)
			val planted = patch.plant(seed)
			if (walkBetween && planted) {
				val doneDidIt = waitForDistance(patch.tile, extraWait = 1800) { !patch.isEmpty(true) }
				script.logger.warn("Planted on $patch: $doneDidIt")
				if (doneDidIt) {
					if (patch.water()) {
						if (patch.index < script.patchCount - 1) {
							seed.interact("Use", false)
						}
						script.waitForTicks(2)
						script.logger.warn("Watered as well.. $patch")
					}

				}
			} else {
				script.logger.warn("Failed to plant: walkBetween=${walkBetween}, planted=$planted")
			}
		}
	}
}