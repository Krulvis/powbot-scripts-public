package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.sleep
import org.powbot.krulvis.mta.rooms.GraveyardRoom
import org.powbot.krulvis.mta.MTA

class TakeBones(script: MTA) : Leaf<MTA>(script, "Grab bones") {
    override fun execute() {
        val pile = GraveyardRoom.getBonesPile()
        val invCount = Inventory.emptySlotCount()
        if (pile.valid() && walkAndInteract(pile, "Grab")) {
            sleep(150, 350)
        }
    }
}