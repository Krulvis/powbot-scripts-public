package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.mta.rooms.EnchantingRoom
import org.powbot.krulvis.mta.MTA

class DepositOrbs(script: MTA) : Leaf<MTA>(script, "Deposit orbs") {
    override fun execute() {
        val droppables = EnchantingRoom.getDroppables()
        if (droppables.isNotEmpty()) {
            droppables.forEach { it.interact("Drop") }
            waitFor(200) { EnchantingRoom.getDroppables().isEmpty() }
        } else {
            val hole = Objects.stream().name("Hole").action("Deposit").firstOrNull() ?: return
            if (walkAndInteract(hole, "Deposit")) {
                waitForDistance(hole) { !Inventory.isFull() }
            }
        }
    }
}