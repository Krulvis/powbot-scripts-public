package org.powbot.krulvis.tithe.tree.branch

import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.SimpleLeaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.tithe.Data
import org.powbot.krulvis.tithe.Patch.Companion.hasDone
import org.powbot.krulvis.tithe.Patch.Companion.hasEmpty
import org.powbot.krulvis.tithe.TitheFarmer
import org.powbot.krulvis.tithe.tree.tree.*

class ShouldRefill(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should refill") {
    override val successComponent: TreeComponent<TitheFarmer> = Refill(script)
    override val failedComponent: TreeComponent<TitheFarmer> = ShouldMoveCamera(script)

    override fun validate(): Boolean {
        script.refreshPatches()
        if (script.startPoints == -1) {
            script.startPoints = script.getPoints()
        } else {
            script.gainedPoints = script.getPoints() - script.startPoints
        }
        if (script.lastLeaf.name != "Waiting...") {
            script.chillTimer.reset()
        }
        println("Found: ${script.patches.size} patches: nill=${script.patches.count { it.isNill }}")
        return ctx.inventory.toStream().list().none { it.id() in Data.WATER_CANS } ||
                (ctx.inventory.toStream().id(Data.EMPTY_CAN).isNotEmpty() && script.patches.all { it.isEmpty() })
    }
}

class ShouldMoveCamera(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should turn camera") {
    override val successComponent: TreeComponent<TitheFarmer> = SimpleLeaf(script, "Moving camera") {
        ctx.camera.angle(Random.nextInt(255, 290))
        ctx.camera.pitch(Random.nextInt(95, 99))
    }

    override val failedComponent: TreeComponent<TitheFarmer> = ShouldHandlePatch(script)

    override fun validate(): Boolean {
        return ctx.camera.yaw() !in 255..290 || ctx.camera.pitch() < 95
    }
}

class ShouldHandlePatch(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should handle patch") {
    override val successComponent: TreeComponent<TitheFarmer> = HandlePatch(script)

    override val failedComponent: TreeComponent<TitheFarmer> = ShouldWalkBack(script)

    override fun validate(): Boolean {
        val hasEnoughWater = script.hasEnoughWater()
        val hasSeeds = script.hasSeeds()
        return script.patches.any { it.needsAction() && ((hasEnoughWater && hasSeeds) || !it.isEmpty()) }
    }
}

//class ShouldWater(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should water") {
//    override val successComponent: TreeComponent<TitheFarmer> = Water(script)
//    override val failedComponent: TreeComponent<TitheFarmer> = ShouldHarvest(script)
//
//    override fun validate(): Boolean {
//        return script.patches.any { it.needsWatering }
//    }
//}
//
//class ShouldHarvest(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should harvest") {
//    override val successComponent: TreeComponent<TitheFarmer> = Harvest(script)
//    override val failedComponent: TreeComponent<TitheFarmer> = ShouldPlant(script)
//
//    override fun validate(): Boolean {
//        return script.patches.hasDone()
//    }
//}
//
//class ShouldPlant(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should plant") {
//    override val successComponent: TreeComponent<TitheFarmer> = Plant(script)
//    override val failedComponent: TreeComponent<TitheFarmer> = ShouldWalkBack(script)
//
//    override fun validate(): Boolean {
//        return script.patches.hasEmpty() && hasEnoughWater()
//    }
//
//    fun getWaterCount(): Int = ctx.inventory.toStream().list().sumBy { it.id() - 5332 }
//
//    fun hasEnoughWater(): Boolean = getWaterCount() >= 24
//}
//
class ShouldWalkBack(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should plant") {
    override val successComponent: TreeComponent<TitheFarmer> = WalkBack(script)
    override val failedComponent: TreeComponent<TitheFarmer> = SimpleLeaf(script, "Waiting...") {}

    override fun validate(): Boolean {
        return script.chillTimer.isFinished()
    }
}





