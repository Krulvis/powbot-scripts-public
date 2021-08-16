package org.powbot.krulvis.miner.tree.leaf

import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.miner.Data
import org.powbot.krulvis.miner.Miner
import org.powerbot.script.rt4.GameObject

class DropPayDirt(script: Miner) : Leaf<Miner>(script, "Drop pay-dirt") {
    override fun execute() {
        val hopper = getHopper()
        val totalPaydirt = ctx.inventory.getCount(Ore.PAY_DIRT.id) + script.getMotherloadCount()
        hopper.ifPresent {
            if (it.distance() > 3) {
                val path = LocalPathFinder.findPath(it.tile())
                println("Walking to hopper first! pathLength=${path.size}")
                path.traverse()
            } else if (interact(it, "Deposit")) {
                script.lastPayDirtDrop = System.currentTimeMillis()
                waitFor(long()) { deposited() }
            }
        }
        if (totalPaydirt >= 81 && deposited()) {
            script.shouldEmptySack = true
        }
    }

    fun deposited() = ctx.inventory.getCount(Ore.PAY_DIRT.id) == 0

    fun getHopper() = ctx.objects.toStream().name("Hopper").action("Deposit").findFirst()
}