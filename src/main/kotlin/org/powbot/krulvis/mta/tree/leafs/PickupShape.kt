package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.mta.rooms.EnchantingRoom
import org.powbot.krulvis.mta.MTA

class PickupShape(script: MTA) : Leaf<MTA>(script, "Pickup Shape") {
    override fun execute() {
        val pile = EnchantingRoom.getBonusPile()
        val invCount = Inventory.emptySlotCount()
        if (pile.valid() && walkAndInteract(pile, "Take-from")) {
            waitForDistance(pile) { Inventory.emptySlotCount() < invCount }
        }
    }
}