package org.powbot.krulvis.thiever.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.Random
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.thiever.Thiever
import org.powbot.krulvis.thiever.tree.leaf.Eat
import org.powbot.krulvis.thiever.tree.leaf.HandleBank
import org.powbot.krulvis.thiever.tree.leaf.OpenPouch
import org.powbot.krulvis.thiever.tree.leaf.Pickpocket
import org.powbot.mobile.script.ScriptManager
import kotlin.math.roundToInt

class ShouldEat(script: Thiever) : Branch<Thiever>(script, "Should Eat") {
    override val successComponent: TreeComponent<Thiever> = Eat(script)
    override val failedComponent: TreeComponent<Thiever> = ShouldOpenCoinPouch(script, ShouldBank(script), 28)

    override fun validate(): Boolean {
        val food = script.food
        return food.inInventory() && (food.canEat() || currentHP() <= 4)
    }
}

class ShouldBank(script: Thiever) : Branch<Thiever>(script, "Should Bank") {
    override val successComponent: TreeComponent<Thiever> = CanDrop(script)
    override val failedComponent: TreeComponent<Thiever> = AtSpot(script)

    override fun validate(): Boolean {
        return Inventory.isFull() || (currentHP() < 8 && !script.food.inInventory()) || (script.dodgyNeck && !script.dodgy.inEquipment())
    }
}

class CanDrop(script: Thiever) : Branch<Thiever>(script, "Can drop") {
    override val successComponent: TreeComponent<Thiever> = SimpleLeaf(script, "Dropping") {
        droppables.forEach {
            it.interact("Drop")
            sleep(Random.nextInt(150, 250))
        }
    }
    override val failedComponent: TreeComponent<Thiever> = ShouldOpenCoinPouch(script, IsBankOpen(script), 1)

    var droppables: List<Item> = emptyList()

    override fun validate(): Boolean {
        if (script.droppables.isEmpty()) return false
        droppables = Inventory.stream().filter { item -> script.droppables.any { item.name().contains(it, true) } }
        return Inventory.isFull() && droppables.isNotEmpty()
    }
}


class ShouldOpenCoinPouch(script: Thiever, nextNode: TreeComponent<Thiever>, private val stackSize: Int) :
        Branch<Thiever>(script, "Should Bank") {
    override val successComponent: TreeComponent<Thiever> = OpenPouch(script)
    override val failedComponent: TreeComponent<Thiever> = nextNode

    override fun validate(): Boolean {
        return script.coinPouchCount() >= stackSize
    }
}

class IsBankOpen(script: Thiever) : Branch<Thiever>(script, "Should Open Bank") {
    override val successComponent: TreeComponent<Thiever> = HandleBank(script)
    override val failedComponent: TreeComponent<Thiever> = SimpleLeaf(script, "Open bank") {
        Bank.openNearest()
    }

    override fun validate(): Boolean {
        return Bank.opened()
    }
}

class AtSpot(script: Thiever) : Branch<Thiever>(script, "AtSpot?") {
    override val successComponent: TreeComponent<Thiever> = ShouldStop(script)
    override val failedComponent: TreeComponent<Thiever> = SimpleLeaf(script, "Walking") {
        Bank.close()
        if (Players.local().tile().floor > 0) {
            script.log.info("Climbing down first...")
            if (walkAndInteract(Objects.stream().action("Climb-down").nearest().firstOrNull(), "Climb-down")) {
                waitFor(long()) { validate() }
            }
        } else {
            Movement.walkTo(script.centerTile)
        }
    }

    override fun validate(): Boolean {
        return script.centerTile.distance() <= 10
                && (script.getTarget()?.distance()?.roundToInt() ?: 99) < 20
    }
}

class ShouldStop(script: Thiever) : Branch<Thiever>(script, "Should stop?") {
    override val successComponent: TreeComponent<Thiever> = SimpleLeaf(script, "Stop because npc wandered") {
        Notifications.showNotification("Stopping script because NPC wandered too far away")
        script.log.info("Stopping script because NPC wandered too far away \n NPC: $target, tile=${target?.tile()}, centerTile=${script.centerTile}, distance=${target?.distanceTo(script.centerTile)}")
        ScriptManager.stop()
    }
    override val failedComponent: TreeComponent<Thiever> = Pickpocket(script)

    var target: Npc? = null

    override fun validate(): Boolean {
        if (!script.stopOnWander || script.centerTile == Tile.Nil) return false
        target = script.getTarget()
        return (target?.tile()?.distanceTo(script.centerTile) ?: 99).toInt() >= script.maxDistance
    }
}
