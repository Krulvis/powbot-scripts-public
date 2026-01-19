package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.DepositBox
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.canReach
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.mid
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.miner.Miner
import kotlin.math.roundToInt

class EmptySack(script: Miner) : Leaf<Miner>(script, "Emptying sack") {
    override fun execute() {
        val sack = script.getSack()
        sack.ifPresent {
            if (Bank.opened()) {
                Bank.close()
            } else if (DepositBox.opened()) {
                DepositBox.close()
            }
            if (!it.canReach()) {
                !script.escapeTopFloor()
            } else if (walkAndInteract(it, "Search")) {
                waitFor(mid() + it.distance().roundToInt() * 400) { Inventory.isFull() }
            }
        }
    }
}
