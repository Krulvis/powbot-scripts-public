package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.Condition.sleep
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.Action
import org.powbot.krulvis.giantsfoundry.GiantsFoundry
import org.powbot.krulvis.giantsfoundry.currentHeat
import kotlin.math.abs
import kotlin.math.min

class FixTemperature(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Fix temperature") {


	override fun execute() {
		val action = script.currentStage.action
		val targetHeat = if (action.heats) action.minHeat + 6 else action.maxHeat - 5
		var lastTemp = currentHeat()
		val shouldCool = lastTemp > targetHeat
		if (shouldCool && lastTemp - targetHeat <= 4) {
			script.logger.info("Only have to cool 2, just wait in front of action object")
			if (action.tile.distance() > 3) {
				Movement.step(action.tile)
			}
			return
		}
		val actionObj =
			Objects.stream(30).type(GameObject.Type.INTERACTIVE).name(if (shouldCool) "Waterfall" else "Lava pool")
				.firstOrNull() ?: return
		val actionStr = if (shouldCool) "Cool-preform" else "Heat-preform"
		if (shouldCool) {
			actionObj.bounds(-102, 61, -264, -150, -122, 62)
		} else {
			actionObj.bounds(-122, 72, -24, 0, -122, 122)
		}

		var lastStepSize = 5
		script.logger.info("Performing: $actionStr, on obj=${actionObj.name}, targetHeat=$targetHeat")
		if (walkAndInteract(actionObj, actionStr)) {
			waitFor { tempStep(lastTemp, currentHeat(), lastStepSize) }
			var lastTempChangeMS = System.currentTimeMillis()

			while (!done(
					action,
					targetHeat,
					shouldCool,
					lastStepSize
				) && System.currentTimeMillis() - lastTempChangeMS <= 3500
			) {
				val ms = System.currentTimeMillis() - lastTempChangeMS
				script.logger.info("Still fixing.... lastTempStep=${ms}ms")
				val newTemp = currentHeat()
				if (tempStep(lastTemp, newTemp, lastStepSize)) {
					lastStepSize = abs(lastTemp - newTemp)
					script.logger.info("Temperature step size=$lastStepSize, $lastTemp -> $newTemp, $ms ago with target=$targetHeat")
					lastTempChangeMS = System.currentTimeMillis()
					lastTemp = newTemp
					if (lastStepSize > 20 && lastStepSize > abs(lastTemp - targetHeat)) {
						script.logger.info("Clicking again we're making BIG steps=$lastStepSize")
						walkAndInteract(actionObj, actionStr)
						lastStepSize = 5
					}
				}
				sleep(150)
			}
			script.logger.info(
				"Stopping FIX" +
					"\n done=${done(action, targetHeat, shouldCool, lastStepSize)}," +
					"\n last temp change was ${System.currentTimeMillis() - lastTempChangeMS} ms ago"
			)
			script.stopActivity(action.tile)
		} else {
			script.logger.info("Failed to even interact to FIX TEMPERATURE on ${actionObj.name}: $actionStr")
		}
	}


	/**
	 * "If the difference between the new temperature and the last temperature is greater than the last step size, return
	 * true."
	 *
	 * Args:
	 *   lastTemp (Int): The last temperature that was recorded.
	 *   newTemp (Int): The temperature of the current iteration.
	 *   lastStepSize (Int): The last step size that was used.
	 */
	private fun tempStep(lastTemp: Int, newTemp: Int, lastStepSize: Int) = abs(newTemp - lastTemp) >= lastStepSize

	private fun done(action: Action, target: Int, cooling: Boolean, lastStepSize: Int): Boolean {
		val currentHeat = currentHeat()
		return if (cooling) {
			currentHeat <= if (action.heats) target + min(10, lastStepSize) else target
		} else {
			currentHeat >= target
		}
	}
}