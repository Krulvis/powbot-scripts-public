package org.powbot.krulvis.woodcutter.tree.branch

import org.powbot.api.Condition.sleep
import org.powbot.api.Random
import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.krulvis.woodcutter.Woodcutter
import org.powbot.krulvis.woodcutter.tree.leaf.Chop
import org.powbot.krulvis.woodcutter.tree.leaf.Walk
import org.powbot.mobile.script.ScriptManager

class HasTools(script: Woodcutter) : Branch<Woodcutter>(script, "Has Tools?") {
    override val failedComponent: TreeComponent<Woodcutter> = SimpleLeaf(script, "Stopping Script") {
        ScriptManager.stop()
    }
    override val successComponent: TreeComponent<Woodcutter> = AtSpot(script)

    override fun validate(): Boolean {
        return Inventory.stream().id(*script.TOOLS).isNotEmpty() || Equipment.stream().id(*script.TOOLS).isNotEmpty()
    }
}

class AtSpot(script: Woodcutter) : Branch<Woodcutter>(script, "At Spot?") {
    override val failedComponent: TreeComponent<Woodcutter> = Walk(script)
    override val successComponent: TreeComponent<Woodcutter> = ShouldSpec(script)

    override fun validate(): Boolean {
        return script.trees.any { it.distance() < 5 }
    }
}


class ShouldSpec(script: Woodcutter) : Branch<Woodcutter>(script, "Should Spec?") {
    override fun validate(): Boolean {
        return Combat.specialPercentage() == 100 && delay.isFinished()
                && Equipment.stream().id(*SPECIAL_ATTACK_AXES).isNotEmpty()
    }

    val SPECIAL_ATTACK_AXES = intArrayOf(
        6739, 25378, 13241, 13242, 25066, 28217, 28220, 25371, 23673, 23675
    )

    val delay = DelayHandler(5000, 35000)

    override val successComponent: TreeComponent<Woodcutter> = SimpleLeaf(script, "Special Attack") {
        if (Combat.specialAttack(true)) {
            delay.resetTimer()
            Utils.waitFor { Combat.specialPercentage() < 100 }
        }
    }
    override val failedComponent: TreeComponent<Woodcutter> = IsChopping(script)
}


class IsChopping(script: Woodcutter) : Branch<Woodcutter>(script, "Is Chopping?") {
    override val failedComponent: TreeComponent<Woodcutter> = Chop(script)
    override val successComponent: TreeComponent<Woodcutter> =
        SimpleLeaf(script, "Chilling") { sleep(Random.nextInt(500, 700)) }

    override fun validate(): Boolean {
        return if (System.currentTimeMillis() - script.lastChopAnim < 1000) {
            true
        } else if (Players.local().animation() != -1) {
            script.lastChopAnim = System.currentTimeMillis()
            true
        } else {
            false
        }
    }
}