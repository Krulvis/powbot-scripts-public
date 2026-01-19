package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.canReach
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.miner.Data.getInvPaydirt
import org.powbot.krulvis.miner.Data.getMotherlodeCapacity
import org.powbot.krulvis.miner.Data.getMotherlodeCount
import org.powbot.krulvis.miner.Miner

class DropPayDirt(script: Miner) : Leaf<Miner>(script, "Drop pay-dirt") {

    override fun execute() {
        val hopper = getHopper()
        val invDirt = getInvPaydirt()
        val totalPaydirt = invDirt + getMotherlodeCount()
        if (hopper == null) {
            Movement.moveTo(script.nearHopperTile)
            return
        }
        if (hopper.canReach()) {
            script.logger.warn("Hopper at ${hopper.tile()}")
            if (walkAndInteract(hopper, "Deposit")) {
                script.lastPayDirtDrop = System.currentTimeMillis()
                sleep(600)
                waitFor(long()) {
                    deposited(invDirt) && canWalkAway()
                }
            }
        } else if (script.inTopFloorAreas() && script.topLevelHopper && !script.inTopFloorAreas(hopper.tile)) {
            script.escapeTopFloor()
            return
        } else if (!script.nearHopperTile.reachable() || script.nearHopperTile.distance() > 15) {
            script.logger.warn("nearHopperTileReachable=${script.nearHopperTile.reachable()}, distance=${script.nearHopperTile.distance()}")
            if (!script.escapeTopFloor()) return
            val path = LocalPathFinder.findPath(script.nearHopperTile)
            if (path.isNotEmpty()) {
                path.traverseUntilReached()
            } else {
                Movement.moveTo(script.nearHopperTile)
            }
        }

        if (totalPaydirt >= getMotherlodeCapacity() && deposited(invDirt)) {
            script.shouldEmptySack = true
        }
    }

    fun canWalkAway(): Boolean {
        return script.getBrokenStrut() == null
                || Npcs.stream().name("Pay-dirt").isNotEmpty()
    }

    fun deposited(originalCount: Int) = Inventory.getCount(Ore.PAY_DIRT.id) < originalCount

    fun getHopper(): GameObject? {
        if (script.topLevelHopper && script.inTopFloorAreas() && (script.getBrokenStrutCount() < 2 || !script.isPayDirtStuck())) {
            return Objects.stream(50).type(GameObject.Type.INTERACTIVE)
                .at(Tile(3755, 5677, 0))
                .name("Hopper").action("Deposit").firstOrNull()
        } else {
            return Objects.stream(50).type(GameObject.Type.INTERACTIVE)
                .at(Tile(3748, 5672, 0)).name("Hopper").action("Deposit").firstOrNull()
        }
    }
}