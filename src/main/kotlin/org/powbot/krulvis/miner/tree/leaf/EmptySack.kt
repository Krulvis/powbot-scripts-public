package org.powbot.krulvis.miner.tree.leaf

import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.mid
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.miner.Miner

class EmptySack(script: Miner) : Leaf<Miner>(script, "Emptying sack") {
    override fun execute() {
        val sack = script.getSack()
        sack.ifPresent {
            if (interact(it, "Search")) {
                waitFor(mid() + it.distance() * 400) { ctx.inventory.isFull }
            }
        }
    }
}