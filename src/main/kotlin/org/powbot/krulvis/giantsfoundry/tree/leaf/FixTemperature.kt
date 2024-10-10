package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.Condition.sleep
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.Action
import org.powbot.krulvis.giantsfoundry.GiantsFoundry
import org.powbot.krulvis.giantsfoundry.currentTemp
import kotlin.math.abs
import kotlin.math.min

class FixTemperature(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Fix temperature") {


	override fun execute() {
		val action = script.currentAction ?: return
		val targetHeat = if (action.heats) action.min + 6 else action.max - 5
		var lastTemp = currentTemp()
		val shouldCool = lastTemp > targetHeat
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
		if (actionObj.use(actionStr)) {
			waitFor { tempStep(lastTemp, currentTemp(), lastStepSize) }
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
				val newTemp = currentTemp()
				if (tempStep(lastTemp, newTemp, lastStepSize)) {
					lastStepSize = abs(lastTemp - newTemp)
					script.logger.info("Temperature step size=$lastStepSize, $lastTemp -> $newTemp, $ms ago with target=$targetHeat")
					lastTempChangeMS = System.currentTimeMillis()
					lastTemp = newTemp
					if (lastStepSize > 20 && lastStepSize > abs(lastTemp - targetHeat)) {
						script.logger.info("Clicking again we're making BIG steps=$lastStepSize")
						actionObj.use(actionStr)
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

	private fun GameObject.use(action: String): Boolean {
		if (distance() >= 5) {
			Movement.step(tile)
			waitFor(long()) { distance() < 5 }
		}
		return interact(action)
	}

	/**
	 * "If the difference between the new temperature and the last temperature is greater than the last step size, return
	 * true."
	 *
	 * The function is called tempStep. It takes three parameters: lastTemp, newTemp, and lastStepSize. It returns a
	 * Boolean value
	 *
	 * Args:
	 *   lastTemp (Int): The last temperature that was recorded.
	 *   newTemp (Int): The temperature of the current iteration.
	 *   lastStepSize (Int): The last step size that was used.
	 */
	private fun tempStep(lastTemp: Int, newTemp: Int, lastStepSize: Int) = abs(newTemp - lastTemp) >= lastStepSize

	private fun done(action: Action, target: Int, cooling: Boolean, lastStepSize: Int): Boolean {
		val currentHeat = currentTemp()
		return if (cooling) {
			currentHeat <= if (action.heats) target + min(10, lastStepSize) else target
		} else {
			currentHeat >= target
		}
	}
}