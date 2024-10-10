package org.powbot.krulvis.thiever.chest

import org.powbot.api.Notifications
import org.powbot.api.Random
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.Leaf
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.tree.branch.ShouldHighAlch
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.mobile.script.ScriptManager

class ShouldClearInv(script: ChestThiever) : Branch<ChestThiever>(script, "Should clear inv?") {
    override val failedComponent: TreeComponent<ChestThiever> = ShouldEat(script)
    override val successComponent: TreeComponent<ChestThiever> = SimpleLeaf(script, "Clear Inventory") {
        sleep(600, 1200)
        if (hasHerbs) herbSack?.interact("Fill")
        trash.forEach {
            it.interact("Drop")
        }
        waitFor { Inventory.stream().id(*script.trash).isEmpty() }
    }

    var trash: List<Item> = emptyList()

    val hasHerbs = Inventory.stream().name("grimy").isNotEmpty()
    val herbSack get() = Inventory.stream().id(*script.herbSacks).firstOrNull()

    override fun validate(): Boolean {
        trash = Inventory.stream().id(*script.trash).list()
        return trash.isNotEmpty() || (herbSack != null && hasHerbs)
    }
}

class ShouldEat(script: ChestThiever) : Branch<ChestThiever>(script, "Should eat?") {
    override val failedComponent: TreeComponent<ChestThiever> = ShouldWalk(script)
    override val successComponent: TreeComponent<ChestThiever> = SimpleLeaf(script, "Eating") {
        script.eatFood()
    }

    var food: Food? = null

    override fun validate(): Boolean {
        if (script.stopping) return false
        val food = Food.getFirstFood()
        val hp = Combat.health()
        val maxHp = Combat.maxHealth()

        return hp <= 20 || (food?.healing ?: 0) < maxHp - hp || Inventory.isFull()
    }

}


class ShouldWalk(script: ChestThiever) : Branch<ChestThiever>(script, "Should walk?") {
    override val failedComponent: TreeComponent<ChestThiever> = ShouldHighAlch(script, ShouldStop(script))
    override val successComponent: TreeComponent<ChestThiever> = SimpleLeaf(script, "Walking") {
        Movement.walkTo(tile)
    }

    val tile = script.chestTile.derive(0, -1)

    override fun validate(): Boolean {
        return tile.distance() > 1
    }
}

class ShouldStop(script: ChestThiever) : Branch<ChestThiever>(script, "Should stop?") {
    override val failedComponent: TreeComponent<ChestThiever> = Picklock(script)
    override val successComponent: TreeComponent<ChestThiever> = SimpleLeaf(script, "Stopping") {
        Notifications.showNotification("Out of food")
        ScriptManager.stop()
    }

    override fun validate(): Boolean {
        return script.stopping
    }
}


class Picklock(script: ChestThiever) : Leaf<ChestThiever>(script, "Picklock") {

    override fun execute() {
        val chest = Objects.stream().at(script.chestTile).name("Chest").action("Picklock").firstOrNull() ?: return
        val xp = Skills.experience(Constants.SKILLS_THIEVING)
        if (chest.interact("Picklock")) {
            sleep(Random.nextInt(2000, 3000))
//            script.logger.info("Waited for skilldrop... success=${waitFor(long()) { xp < Skills.experience(Constants.SKILLS_THIEVING) }}")
        }
    }
}