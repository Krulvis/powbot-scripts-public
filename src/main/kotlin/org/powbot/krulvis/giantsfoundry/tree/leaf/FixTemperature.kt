package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.Condition.sleep
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Widgets
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.*
import org.powbot.krulvis.giantsfoundry.data.Stage
import kotlin.math.abs
import kotlin.math.max

class FixTemperature(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Fix temperature") {

    override fun execute() {
        val stage = script.currentStage
        val action = stage.action
        var lastTemp = currentHeat()
        val targetHeat = calculateTargetHeat(stage, lastTemp)
        val shouldCool = lastTemp > targetHeat

        val barComp = Widgets.component(ROOT, 8)
        val painter = (script.painter as GiantsFoundryPainter)
        val screenpoint = barComp.screenPoint()
        painter.targetHeatX = (screenpoint.x + targetHeat / 1000.0 * barComp.width()).toInt()
        painter.targetHeatY = screenpoint.y - 2
        script.logger.warn("Target heat=$targetHeat, currentTemp=$lastTemp, shouldCool=$shouldCool")

        if (shouldCool && lastTemp - targetHeat <= 5) {
            script.logger.warn("Only have to cool ${lastTemp - targetHeat}, just wait in front of action object")
            if (action.tile.distance() > 2) {
                Movement.step(action.tile)
            }
            val canPerform = waitFor { script.currentStage.action.canPerform() }
            script.logger.warn("Can perform? $canPerform")
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
        script.logger.warn("Performing: $actionStr, on obj=${actionObj.name}, targetHeat=$script.targetHeat")
        if (walkAndInteract(actionObj, actionStr, useMenu = false)) {
            waitFor { madeIncreasedStep(lastTemp, currentHeat(), lastStepSize) }
            var lastTempChangeMS = System.currentTimeMillis()
            val timeout = Timer(15000)
            while (!done(targetHeat, shouldCool)
                && !timeout.isFinished()
            ) {
                val ms = System.currentTimeMillis() - lastTempChangeMS
                val newTemp = currentHeat()
                if (madeIncreasedStep(lastTemp, newTemp, lastStepSize)) {
                    lastStepSize = abs(lastTemp - newTemp)
                    script.logger.warn("Temperature step size=$lastStepSize, $lastTemp -> $newTemp, $ms ago with target=$script.targetHeat")
                    lastTempChangeMS = System.currentTimeMillis()
                    lastTemp = newTemp
                    if (lastStepSize > 5 && lastStepSize >= abs(newTemp - targetHeat)) {
                        script.logger.warn("Clicking again we're making BIG steps=$lastStepSize")
                        walkAndInteract(actionObj, actionStr, useMenu = false)
                        lastStepSize = 5
                    }
                }
                sleep(50)
            }
            script.logger.warn(
                "Stopping FIX" +
                        "\n done=${done(targetHeat, shouldCool)}," +
                        "\n last temp change with size=${lastStepSize} was ${System.currentTimeMillis() - lastTempChangeMS} ms ago"
            )
            script.stopActivity(if (action.canPerform()) action.tile else null)
        } else {
            script.logger.warn("Failed to even interact to FIX TEMPERATURE on ${actionObj.name}: $actionStr")
        }
    }

    private fun calculateTargetHeat(stage: Stage, currentHeat: Int): Int {
        val action = stage.action
        val progressRemaining = stage.end - currentProgress()
        val barLengthRequired = max(5.0, progressRemaining * 1.8)
        val progressPotential = action.maxHeat - action.minHeat
        script.logger.warn("Fixing temperature, progressRemaining=$progressRemaining, barLengthRequired=$barLengthRequired")
        return if (action.heats) {
            val coldestPoint = action.minHeat + 5
            if (currentHeat > coldestPoint && barLengthRequired < progressPotential) {
                // We need to cool down and we don't need to hit the start point
                max((action.maxHeat - barLengthRequired).toInt(), coldestPoint)
            } else {
                coldestPoint
            }
        } else if (currentHeat > action.maxHeat || barLengthRequired >= progressPotential) {
            //Cooling time so just go to max
            action.maxHeat
        } else {
            //Need to heat only a lil bit
            (action.minHeat + barLengthRequired).toInt()
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
    private fun madeIncreasedStep(lastTemp: Int, newTemp: Int, lastStepSize: Int) =
        abs(newTemp - lastTemp) >= lastStepSize

    /**
     * Determines whether the heating or cooling process has reached its target based on
     * the current heat, target value, and the last step size.
     *
     * @param action The current action being performed, which may influence the heating process.
     * @param target The target heat level to determine completion.
     * @param cooling Indicates whether the process is focused on cooling or heating.
     */
    private fun done(target: Int, cooling: Boolean): Boolean {
        val currentHeat = currentHeat()
        return if (cooling) {
            currentHeat <= target
        } else {
            currentHeat >= target
        }
    }
}