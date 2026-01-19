package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.miner.Miner

class DropSandstone(script: Miner) : Leaf<Miner>(script, "Drop sandstone") {

    override fun execute() {
        val hopper = grinder()
        if (hopper != null && Utils.walkAndInteract(hopper, "Deposit")) {
            waitFor { !Inventory.isFull() }
        }
    }
    
    fun grinder() = Objects.stream(50).type(GameObject.Type.INTERACTIVE).name("Grinder").action("Deposit").firstOrNull()
}